package cn.sonshou.wsinvoker.lang2.soapmessage.soap11;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLOperation;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParam;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParamType;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParamTypeField;
import cn.sonshou.wsinvoker.lang2.soapmessage.ASOAPHandler;
import cn.sonshou.wsinvoker.lang2.util.TypeCompareUtil;
import cn.sonshou.wsinvoker.lang2.util.namespace.NameSpaceUtil;
import cn.sonshou.wsinvoker.lang2.util.namespace.Namespace;
import cn.sonshou.wsinvoker.lang2.util.ResponseException;
import cn.sonshou.wsinvoker.lang2.w3c.bindingType.ABindingType;

public class SOAP11DocumentLiteralSOAPHandler extends ASOAPHandler
{
	public SOAP11DocumentLiteralSOAPHandler(ABindingType bindingType) {
		super(bindingType);
	}
	
	protected void createSoapBodyContent(SOAPEnvelope envelope, JWSDLOperation operation,
			Namespace[] namespaces) throws Exception 
	{	
		SOAPElement body = envelope.getBody();
		/*nemespace*/
		Map namespaceMap = NameSpaceUtil.toMap(namespaces);
		/*document/literal wrapper 需要构造wrapper头*/
		QName wrapper = operation.getInputWrapper();
		if(wrapper!=null){
			String local = wrapper.getLocalPart();
			String uri = wrapper.getNamespaceURI();
			String prefix = (String)namespaceMap.get(uri);
			Name soapName = envelope.createName(local, prefix, uri);
			/*wrapper的element构造*/
			body = body.addChildElement(soapName);
		}
		
		/*document/literal不需要构造方法名*/
		JWSDLParam[] inputParam = operation.getInputParam();
		for(int i=0; inputParam!=null&&i<inputParam.length; i++)
		{
			QName paramName = inputParam[i].getParamName();
			this.processComplexType(envelope, namespaceMap, body, paramName,
					inputParam[i].getType(), inputParam[i].getValue(), false);
		}
	}
	
	/**
	 * Document/Literal 非Wrapper的返回构造.返回内容直接就在Body中,结构不一定良好
	 */
	protected Object processReturnMessageBody(SOAPBody body, 
			JWSDLParam outputParam, Class clazz) throws ResponseException {
		if(outputParam==null||clazz==null){
			return null;
		}
		/*返回结果可能是通过了Schema定义的N次包装而返回,这里就去找,是否存在一个可匹配的返回类型
		 * 返回的数据不管如何,只可能要么是数组,要么是对象,(均包含简单类型)*/
		boolean isMatch;
		List<JWSDLParamType> debuglist = new ArrayList<>();
		JWSDLParamType type = outputParam.getType();
		debuglist.add(type);
		/*通过一个while,一层一层的剥开判断,最后始终会出现要么是对象要么是数组的情况*/
		isMatch = TypeCompareUtil.isParamTypeMatch(type, clazz, "base");
		QName paramName = outputParam.getParamName();
		while(!isMatch){
			JWSDLParamTypeField[] fields = type.getDeclaredFields();
			if(fields == null || (fields.length < 1)){
				break;
			}
			if(fields.length > 0){
				//取第一个,而且实际上对于返回参数来说,包装的类容作为字段,也应该只有一个
				paramName = fields[0].getQname();
				type = fields[0].getType();
				debuglist.add(type);
				isMatch = TypeCompareUtil.isParamTypeMatch(type, clazz, "base");
				if(type.isPrimitive()){
					/*特别注意这个if,如果一个返回类型最后已经判断到是简单类型了,则说明判断完了*/
					break;
				}
			}
		}
		if(!isMatch){
			StringBuilder debugInfo =  new StringBuilder("返回类型匹配错误: java类型:").append(clazz)
		       .append("\n               wsdl类型(包装结构)-\n");
            for (JWSDLParamType aDebuglist : debuglist) {
                debugInfo.append("                 |_").append(aDebuglist).append("\n");
            }
			return debugInfo.toString();
		}

		/*WS的返回,要么是简单类型;要么是对象;要么是数组,就这几种可能*/
		try{
			if(type.isArray()){
				/*数组的处理,先找到数组元素*/
				NodeList w3cNodelist = body.getElementsByTagNameNS(paramName.getNamespaceURI(),
						paramName.getLocalPart());
				if(w3cNodelist==null||w3cNodelist.getLength()<1){
					w3cNodelist = body.getElementsByTagName(paramName.getLocalPart());
				}
				if(w3cNodelist!=null&&w3cNodelist.getLength()>0){
                    List<Element> elements = new ArrayList<>();
                    for(int m=0; m<w3cNodelist.getLength(); m++){
                        Element element = (Element)w3cNodelist.item(m);
                        if(element.getParentNode().isSameNode(body)){
                            elements.add(element);
                        }
                    }
                    if(elements.size()>0) {
                        return this.treasArrayType(type, clazz, elements);
                    }else{
                        return null;
                    }
				}else{
					throw new Exception("WS返回内容没有节点:"+paramName);
				}
			}else{
				/*简单类型和复杂类型都统一处理.因为节点是聚合一起的,可以看到这和wrapped的有区别,因为
				 * 我目前所调用的document的返回,都不是按照Schema的定义来返回的.完全乱的*/
				Element w3cNode = null;
				NodeList w3cNodelist = body.getElementsByTagNameNS(paramName.getNamespaceURI(),
						paramName.getLocalPart());
				if(w3cNodelist==null||w3cNodelist.getLength()<1){
					w3cNodelist = body.getElementsByTagName(paramName.getLocalPart());
				}
				if(w3cNodelist!=null&&w3cNodelist.getLength()>0){
					for(int i=0; i<w3cNodelist.getLength(); i++){
						w3cNode = (Element)w3cNodelist.item(i);
						if(w3cNode!=null) break;
					}
				}
					
				if(w3cNode!=null){
                    return this.treasComplexObject(type, clazz, w3cNode);
				}else{
					throw new Exception("WS返回内容没有节点:"+paramName);
				}
			}
		}catch(Exception e){
			throw new ResponseException(e);
		}
	}
}
