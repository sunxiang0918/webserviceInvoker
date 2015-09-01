package cn.sonshou.wsinvoker.lang2.util.namespace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLOperation;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParam;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParamType;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParamTypeField;
import cn.sonshou.wsinvoker.lang2.w3c.Constant;
import cn.sonshou.wsinvoker.lang2.w3c.bindingType.ABindingType;

/**
 * 分析出一个WSDL中operation相关的namespace并设置好前缀
 * @author sonshou
 */
public class NameSpaceUtil 
{	
	public static NameSpaceUtil newInstance(){
		return new NameSpaceUtil();
	}
	
	int i=0;
	private int getNextSequence(){
		return i++;
	}
	
	/**
	 * 本来我是建立一个叫Namespace(dom4j中也有类似的对象)的对象来保存.但为了之后处理方便,直接使用Map了.
	 * 一般来说对于集合的返回,如果是不能采用泛型,最好是一对象数组的形式来处理.
	 * @param operation
	 * @return
	 */
	public Namespace[] analyzeNamespaces(JWSDLOperation operation, ABindingType bindingType)
	{
		Map<String,String> namespaceMap = new HashMap<>();
		namespaceMap.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
		namespaceMap.put("http://www.w3.org/2001/XMLSchema", "xsd");
		namespaceMap.put("http://www.w3.org/2004/12/addressing", "wsa");
		
		if(operation!=null){
			/*首先分析方法中的*/
			namespaceMap.put(operation.getNamespace(),
                    "ns" + getNextSequence());
			
			/*分析参数中的*/
			JWSDLParam[] jwsdlParams = operation.getInputParam();
			if(jwsdlParams!=null){
                for (JWSDLParam jwsdlParam : jwsdlParams) {
                    QName qname = jwsdlParam.getParamName();
                    String spaceurl = qname.getNamespaceURI();
                    if (spaceurl != null && spaceurl.length() > 0 && !namespaceMap.containsKey(spaceurl)) {
                        namespaceMap.put(spaceurl,
                                "ns" + getNextSequence());
                    }

                    JWSDLParamType jwsdlParamType = jwsdlParam.getType();
                    analyzeJWSDLTypeNameSpace(namespaceMap, jwsdlParamType, new ArrayList<QName>());
                }
			}
		}
		
		Namespace[] namespaces = new Namespace[namespaceMap.size()];
		Iterator itr = namespaceMap.keySet().iterator();
		int i=0;
		while(itr.hasNext()){
			String uri = (String)itr.next();
			String prefix = namespaceMap.get(uri);
			namespaces[i] = new Namespace(prefix, uri);
			i++;
		}
		return namespaces;
	}
	
	private void analyzeJWSDLTypeNameSpace(Map<String,String> currentCache, JWSDLParamType jwsdlParamType, List<QName> stack)
	{
		/*分析参数的类型的,如果没有(wrapper的数组是特殊处理,没有),则不处理
		 */
        QName qname = jwsdlParamType.getQname();
        if(stack.contains(qname)) return;

		if(qname!=null&&qname.getNamespaceURI().length()>0&&!currentCache.containsKey(qname.getNamespaceURI())){
			currentCache.put(qname.getNamespaceURI(),
                    "ns" + getNextSequence());
		}

        stack.add(qname);
		
		/*处理数组*/
		if(jwsdlParamType.isArray()){
			JWSDLParamType jwsdlArrayType = jwsdlParamType.getArrayElementType();
            if(!jwsdlArrayType.getQname().equals(qname)) {
                analyzeJWSDLTypeNameSpace(currentCache, jwsdlArrayType, stack);
            }
		}else if(!jwsdlParamType.isPrimitive()&&!jwsdlParamType.getJavaType().equals(Constant.Choice.class)){
			/*复杂类型*/
			JWSDLParamTypeField[] declaredFields = jwsdlParamType.getDeclaredFields();
            for (JWSDLParamTypeField declaredField : declaredFields) {
                QName fieldQName = declaredField.getQname();
                if (fieldQName != null && fieldQName.getNamespaceURI().length() > 0 && !currentCache.containsKey(fieldQName.getNamespaceURI())) {
                    currentCache.put(fieldQName.getNamespaceURI(),
                            "ns" + getNextSequence());
                }
                /*嵌套类型不需要再处理*/
                if (!declaredField.getType().getQname().equals(qname)
                        ||
                        !(declaredField.getType().isArray()
                                && declaredField.getType().getArrayElementType() != null
                                && declaredField.getType().getArrayElementType().getQname().equals(qname))) {
                    analyzeJWSDLTypeNameSpace(currentCache, declaredField.getType(), stack);
                }
            }
		}
	}
	
	public static Map<String,String> toMap(Namespace[] namespaces)
	{
		Map<String,String> map = new HashMap<>();
        for (Namespace namespace : namespaces) {
            map.put(namespace.getUri(), namespace.getPrefix());
        }
		return map;
	}
}
