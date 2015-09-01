package cn.sonshou.wsinvoker.lang2.soapmessage.soap11;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParam;
import cn.sonshou.wsinvoker.lang2.soapmessage.ASOAPHandler;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLOperation;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParamType;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParamTypeField;
import cn.sonshou.wsinvoker.lang2.soapmessage.AnyTypeObject;
import cn.sonshou.wsinvoker.lang2.util.PrimitiveTypeUtil;
import cn.sonshou.wsinvoker.lang2.util.ResponseException;
import cn.sonshou.wsinvoker.lang2.util.TypeCompareUtil;
import cn.sonshou.wsinvoker.lang2.util.namespace.NameSpaceUtil;
import cn.sonshou.wsinvoker.lang2.util.namespace.Namespace;
import cn.sonshou.wsinvoker.lang2.w3c.bindingType.ABindingType;

/**
 * SOAP11下RPC-ENCODE方式的SOAP构造.需要构造xsd的类型描述
 * @author sonshou
 */
public class SOAP11RPCEncodeSOAPHandler extends ASOAPHandler
{
	public SOAP11RPCEncodeSOAPHandler(ABindingType bindingType) {
		super(bindingType);
	}
	
	protected void createSoapBodyContent(SOAPEnvelope envelope, JWSDLOperation operation,
			Namespace[] namespaces) throws Exception {
		SOAPElement body = envelope.getBody();
		/*nemespace*/
		Map namespaceMap = NameSpaceUtil.toMap(namespaces);
		/*RPC 需要构造方法名*/
		String local = operation.getLocalName();
		String uri = operation.getNamespace();
		String prefix = (String)namespaceMap.get(uri);
		Name soapName = envelope.createName(local, prefix, uri);
		/*wrapper的element构造*/
		SOAPElement methodNameElement = body.addChildElement(soapName);
		JWSDLParam[] inputParam = operation.getInputParam();
		for(int i=0; inputParam!=null&&i<inputParam.length; i++)
		{
			QName paramName = inputParam[i].getParamName();
			this.processComplexType(envelope, namespaceMap, methodNameElement, paramName,
					inputParam[i].getType(), inputParam[i].getValue(), isBindType());
		}
	}
	
	protected boolean isBindType()
	{
		return true;
	}
	
	/**
	 * 处理RPC的返回
	 */
	private static ThreadLocal<SOAPBody> local = new ThreadLocal<>();
	protected Object processReturnMessageBody(SOAPBody body, 
			JWSDLParam outputParam, Class clazz) throws ResponseException {
		if (clazz==null) {
			return "返回类型匹配错误:Java类型为空";
		}
		local.set(body);
		/*和document一样,总之不是数组就是对象,为了适应AXIS(因为只有AXIS支持RPC-Encode),这里有点特殊处理*/
		JWSDLParamType type = outputParam.getType();
		boolean isMatch = TypeCompareUtil.isParamTypeMatch(type, clazz, "base");
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
				isMatch = TypeCompareUtil.isParamTypeMatch(type, clazz, "base");
				if(type.isPrimitive()){
					/*特别注意这个if,如果一个返回类型最后已经判断到是简单类型了,则说明判断完了*/
					break;
				}
			}
		}
		if(!isMatch){
			return "返回类型匹配错误: java类型:"+clazz+" wsdl类型-"+outputParam.getType();
		}
		try{
			if(outputParam.getType().isArray()){
				/*数组的处理,先找到数组元素,和dcoument不一样,RPC的数组元素节点名是乱取的,因此需找到数组节点*/
				NodeList w3cNodelist = body.getElementsByTagNameNS(paramName.getNamespaceURI(),
							paramName.getLocalPart());
				if(w3cNodelist==null||w3cNodelist.getLength()<1){
					w3cNodelist = body.getElementsByTagName(paramName.getLocalPart());
				}
				if(w3cNodelist!=null&&w3cNodelist.getLength()>0){
					Element array = (Element)w3cNodelist.item(0);
					NodeList rpcArrayElementList = array.getChildNodes();
					if(rpcArrayElementList!=null&&rpcArrayElementList.getLength()>0){
						Object arrayReturn = Array.newInstance(clazz.getComponentType(),
								rpcArrayElementList.getLength());
						for(int i=0; i<rpcArrayElementList.getLength(); i++){
							Element soapBodyArrayElement = (Element)rpcArrayElementList.item(i);//每个数组节点
							/*特殊处理AXIS的返回*/
							NodeList childList = soapBodyArrayElement.getChildNodes();
							if(childList==null||childList.getLength()<1){
								String href = soapBodyArrayElement.getAttribute("href");
								href = href.replaceFirst("#", "");
								soapBodyArrayElement = this.findAXISRPCRefElement(href, body);
							}
							Object arrayI = this.treasComplexObject(outputParam.getType().getArrayElementType(),
									clazz.getComponentType(), soapBodyArrayElement);
							Array.set(arrayReturn, i, arrayI);
						}
						return arrayReturn;
					}
				}
			}else{
				/*简单类型和复杂类型都统一处理.因为节点是聚合一起的*/
				NodeList w3cNodelist = body.getElementsByTagNameNS(paramName.getNamespaceURI(),
						paramName.getLocalPart());
				if(w3cNodelist==null||w3cNodelist.getLength()<1){
					w3cNodelist = body.getElementsByTagName(paramName.getLocalPart());
				}
				if(w3cNodelist!=null&&w3cNodelist.getLength()>0){
					Element w3cNode = (Element)w3cNodelist.item(0);
					NodeList childList = w3cNode.getChildNodes();
					if(childList==null||childList.getLength()<1){
						String href = w3cNode.getAttribute("href");
						href = href.replaceFirst("#", "");
						w3cNode = this.findAXISRPCRefElement(href, body);
					}
					return this.treasComplexObject(outputParam.getType(), clazz, w3cNode);
				}
			}
		}catch(Exception e){
			throw new ResponseException(e);
		}finally{
			local.remove();
		}
		return null;
	}
	
	@Override
	protected Object treasComplexObject(JWSDLParamType type, Class clazz, Element w3cElement) throws Exception
	{
		if(w3cElement==null||clazz==null){
			if(logger.isDebugEnabled()) logger.debug(type.getQname().toString() + "(" + clazz + ")" + " has no SOAPElement to transform");
			return null;
		}
		
		if(type.isPrimitive()){
			/*普通类型,直接构造出结果返回*/
			String value = w3cElement.getTextContent();
			return PrimitiveTypeUtil.createPrimitiveObject(clazz, value);
		}
		else if(type.getQname().getLocalPart().equalsIgnoreCase("anyType")){
			return this.treasAnyType(w3cElement);
		}else{
			/*对象类型*/
			Object returnObject = clazz.newInstance();
			JWSDLParamTypeField[] jwsdlFields = type.getDeclaredFields();
			/*设置对象属性的值*/
			for(int i=0; i<jwsdlFields.length; i++){
				QName jwsdlFieldQName = jwsdlFields[i].getQname();//取得WSDL对象字段描述
				Field apiField = clazz.getDeclaredField(jwsdlFieldQName.getLocalPart());//本地类的Field
				if(apiField!=null){
					if(logger.isDebugEnabled()) logger.debug(
                            "处理返回结果, 字段:" + apiField.getName()
					);
					Object fieldValue = null;

					NodeList nodeList = w3cElement.getElementsByTagNameNS(jwsdlFieldQName.getNamespaceURI(),
							jwsdlFieldQName.getLocalPart());
					if(nodeList==null||nodeList.getLength()<1){
						nodeList = w3cElement.getElementsByTagName(jwsdlFieldQName.getLocalPart());
					}
					if(nodeList!=null&&nodeList.getLength()>0){
						if(jwsdlFields[i].getType().isArray()){
							Element array = (Element)nodeList.item(0);
							NodeList rpcArrayElementList = array.getChildNodes();
							if(rpcArrayElementList!=null&&rpcArrayElementList.getLength()>0){
                                List<Element> elements = new ArrayList<Element>();
                                for(int m=0; m<rpcArrayElementList.getLength();m++){
                                    Element element = (Element)rpcArrayElementList.item(i);
                                    if(element.getNodeType()!= Node.TEXT_NODE){
                                        elements.add(element);
                                    }
                                }
								fieldValue = treasArrayType(jwsdlFields[i].getType(), 
											apiField.getType(), elements);
							}
						}else{
							Element fieldElement = (Element)nodeList.item(0);
							NodeList childList = fieldElement.getChildNodes();
							if(childList==null||childList.getLength()<1){
								String href = fieldElement.getAttribute("href");
								href = href.replaceFirst("#", "");
								fieldElement = this.findAXISRPCRefElement(href, local.get());
							}
							fieldValue = treasComplexObject(jwsdlFields[i].getType(), 
										apiField.getType(), fieldElement);
						}
					}
					apiField.setAccessible(true);
					apiField.set(returnObject, fieldValue);
				}
			}
			return returnObject;
		}
	}
	
	@Override
	protected Object treasArrayType(JWSDLParamType arrayType,  
			Class arrayClass, List<Element> arrayNodeList) throws Exception{
		JWSDLParamType arrayElementType = arrayType.getArrayElementType();
		if(arrayElementType==null){
			throw new Exception("WSDL元素类型"+arrayType+"是数组,但没有数组元素的类型描述");
		}
		Class elementClass = arrayClass.getComponentType();
		Object returnObject = Array.newInstance(elementClass, arrayNodeList.size());
		for(int i=0; i<arrayNodeList.size(); i++){
			Element node = arrayNodeList.get(i); //每个数组对象
			NodeList childList = node.getChildNodes();
			if(childList==null||childList.getLength()<1){
				String href = node.getAttribute("href");
				href = href.replaceFirst("#", "");
				node = this.findAXISRPCRefElement(href, local.get());
			}
			Object fieldValue = treasComplexObject(arrayElementType, elementClass, node);
			Array.set(returnObject, i, fieldValue);
		}
		return returnObject;
	}
	
	private AnyTypeObject treasAnyType(Element w3cElement)
	{
		AnyTypeObject anyType = new AnyTypeObject();
		anyType.setName(w3cElement.getNodeName());
		if(w3cElement.hasChildNodes()){
			NodeList childs = w3cElement.getChildNodes();
			AnyTypeObject[] value = new AnyTypeObject[childs.getLength()];
			for(int i=0; i<childs.getLength(); i++){
				Element childElement = (Element)childs.item(i);
				value[i] = treasAnyType(childElement);
			}
			anyType.setChild(value);
		}else{	
			anyType.setValue(w3cElement.getTextContent());
		}
		return anyType;
	}
	
	private Element findAXISRPCRefElement(String href, Element w3cElement)
	{
		NodeList nodeList = w3cElement.getChildNodes();
		for(int i=0; i<nodeList.getLength(); i++){
			Element node = (Element)nodeList.item(i);
			if(node.hasAttribute("id")){
				String idValue = node.getAttribute("id");
				if(idValue.equalsIgnoreCase(href)){
					return node;
				}
			}
		}
		return null;
	}
}
