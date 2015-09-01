package cn.sonshou.wsinvoker.lang2.soapmessage;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParam;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParamTypeChoiceField;
import cn.sonshou.wsinvoker.lang2.w3c.Constant;
import cn.sonshou.wsinvoker.lang2.w3c.W3CDataTypeConstant;
import cn.sonshou.wsinvoker.lang2.wsa.endpointreference.EndpointReference;
import cn.sonshou.wsinvoker.lang2.wsa.vo.WSATo;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLOperation;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParamType;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParamTypeField;
import cn.sonshou.wsinvoker.lang2.util.PrimitiveTypeUtil;
import cn.sonshou.wsinvoker.lang2.util.ResponseException;
import cn.sonshou.wsinvoker.lang2.util.namespace.Namespace;
import cn.sonshou.wsinvoker.lang2.w3c.bindingType.ABindingType;
import cn.sonshou.wsinvoker.lang2.wsa.WSAConstant;
import cn.sonshou.wsinvoker.lang2.wsa.endpointreference.ReferenceProperty;
import cn.sonshou.wsinvoker.lang2.wsa.vo.WSAAction;
import cn.sonshou.wsinvoker.lang2.wsa.vo.WSAMessageID;
import cn.sonshou.wsinvoker.lang2.wsa.vo.WSARelatesTo;
import cn.sonshou.wsinvoker.lang2.wsa.vo.WSAReplyTo;
import cn.sonshou.wsinvoker.lang2.wsa.vo.WSAddressing;

public abstract class ASOAPHandler 
{
    protected static Logger logger = LoggerFactory.getLogger(ASOAPHandler.class);
	protected ABindingType bindingType;
	public ASOAPHandler(ABindingType bindingType){
		this.bindingType = bindingType;
	}
	
	/*由于ASOAPHandler是单例(@see SOAPHandlerFactory),所以这里使用ThreadLocal来保存变量*/
	private static ThreadLocal<WSAddressing> addressingLocal = new ThreadLocal<WSAddressing>();
	
	public ASOAPHandler addWSAddressing(WSAddressing addressing){
		addressingLocal.set(addressing);
		return this;
	}
	
	private static String[] sunSaajClasses = new String[]{
		"com.sun.xml.messaging.saaj.soap.ver1_2.SOAPMessageFactory1_2Impl",
		"com.sun.xml.messaging.saaj.soap.ver1_1.SOAPMessageFactory1_1Impl",
		"com.sun.xml.messaging.saaj.soap.SAAJMetaFactoryImpl"
	};
	private static String[] sunclasses = new String[]{
		"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl"
	};
	private static boolean validateRuntimeEnv(String[] classNames) throws ClassNotFoundException{
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try{
            for (String className : classNames) {
                try {
                    Class.forName(className);
                    return true;
                } catch (ClassNotFoundException e) {
                    if (classLoader != null) {
                        classLoader.loadClass(className);
                        return true;
                    }
                }
            }
			return false;
		}catch(ClassNotFoundException e){
			return false;
		}
	}
	
	public SOAPMessage createSoapMessage(JWSDLOperation operation, Namespace[] namespaces) throws Exception
	{
		/*开始调用SAAJ-API进行调用,这里引用的是sun saaj 1.3,当然apache,cxf,oralcews都有对应实现*/
		/*SOAP12的创建方式 MessageFactory mf12 = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);*/
	
		/*2011-05-09 对于Transformer的获取,要么采用标准JDK1.5中的com.sun.org.apche.xalan...
		 * 要么采用xalan中的org.apache.xalan...,应对非sun标准JDK1.5的情况,比如IBMJDK*/
		WSAddressing addressing;
		try {
			boolean isSunTransformer = validateRuntimeEnv(sunclasses);
			if(isSunTransformer){
				System.setProperty("javax.xml.transform.TransformerFactory", "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
			}else{
				logger.warn("SAAJ TRANSFORMER CAN NOT FIND \"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl\"" +
				", Try to use Xalan");
				System.setProperty("javax.xml.transform.TransformerFactory", "org.apache.xalan.xsltc.trax.TransformerFactoryImpl");
			}
			
			MessageFactory factory;
			/**
			 * 2011-05-09 sonshou 改为:默认按sun实现,如果sun找不到,则采用容器或其他环境提供的saaj处理
			 */
			boolean sunSaajAvliable = validateRuntimeEnv(sunSaajClasses);	
			if(sunSaajAvliable){
				String protocol = SOAPConstants.SOAP_1_1_PROTOCOL;
				if(this.bindingType.equals(ABindingType.SOAP12)){
					protocol = SOAPConstants.SOAP_1_2_PROTOCOL;
					System.setProperty("javax.xml.soap.MessageFactory",    
						    "com.sun.xml.messaging.saaj.soap.ver1_2.SOAPMessageFactory1_2Impl");
				}else{
					System.setProperty("javax.xml.soap.MessageFactory",    
							"com.sun.xml.messaging.saaj.soap.ver1_1.SOAPMessageFactory1_1Impl");  
				}
				System.setProperty("javax.xml.soap.MetaFactory", "com.sun.xml.messaging.saaj.soap.SAAJMetaFactoryImpl");
				factory = MessageFactory.newInstance(protocol);
			}else{
				factory = MessageFactory.newInstance();
				logger.warn("SAAJ ATTEMP TO USE SUN-IMPL, BUT CAN NOT LOAD \"com.snu.xml.messaging...\"");
			}
			
			SOAPMessage message = factory.createMessage();
			SOAPPart part = message.getSOAPPart();
			SOAPEnvelope envelope = part.getEnvelope();
			/*将命名空间全部加到SOAPEnvelope节点中*/
            for (Namespace namespace : namespaces) {
                String namespaceURI = namespace.getUri();
                String namespacePrefix = namespace.getPrefix();
                envelope.addNamespaceDeclaration(namespacePrefix, namespaceURI);
            }
			/*设置WSA信息*/
			addressing = addressingLocal.get();
			if(addressing!=null){
				this.createWSAddressingInfo(envelope, addressing);
			}
			/*设置Body中的内容*/
			this.createSoapBodyContent(envelope, operation, namespaces);

			message.saveChanges();
			return message;
		}finally{
			addressing = null;
			addressingLocal.remove(); //清除缓存,让GC处理内存
		}
		
	}
	
	/**
	 * 设置ws-addressing的内容
	 * @param envelope
	 * @param addressing
	 * @throws Exception
	 */
	private void createWSAddressingInfo(SOAPEnvelope envelope, WSAddressing addressing) throws Exception
	{
		SOAPHeader header = envelope.getHeader();
		WSAMessageID messageID = addressing.getMessageID();
		envelope.addNamespaceDeclaration("wsa", WSAConstant.namespace);
		if(messageID!=null&&messageID.getContent()!=null){
			Name name = envelope.createName(WSAConstant.WSA_MESSAGEID, "wsa", WSAConstant.namespace);
			SOAPHeaderElement messageIdElement = header.addHeaderElement(name);
			messageIdElement.setValue(messageID.getContent());
		}
		
		WSARelatesTo relatesTo = addressing.getRelatesTo();
		if(relatesTo!=null&&relatesTo.getContent()!=null){
			Name name = envelope.createName(WSAConstant.WSA_RELATESTO, "wsa", WSAConstant.namespace);
			SOAPHeaderElement messageIdElement = header.addHeaderElement(name);
			messageIdElement.setValue(relatesTo.getContent());
		}
		
		WSAAction action = addressing.getAction();
		if(action!=null&&action.getContent()!=null){
			Name name = envelope.createName(WSAConstant.WSA_ACTION, "wsa", WSAConstant.namespace);
			SOAPHeaderElement actionElement = header.addHeaderElement(name);
			actionElement.setValue(action.getContent());
		}
		
		WSAReplyTo replyTo = addressing.getReplyTo();
		if(replyTo!=null&&replyTo.getAddress()!=null&&replyTo.getAddress().getContent()!=null){
			Name name = envelope.createName(WSAConstant.WSA_REPLYTO, "wsa", WSAConstant.namespace);
			SOAPHeaderElement replyToElement = header.addHeaderElement(name);
			name = envelope.createName(WSAConstant.WSA_ADDRESS, "wsa", WSAConstant.namespace);
			replyToElement.addChildElement(name).setValue(replyTo.getAddress().getContent());
		}

		WSATo to = addressing.getTo();
		if(to!=null&&to.getContent()!=null){
			Name name = envelope.createName(WSAConstant.WSA_TO, "wsa", WSAConstant.namespace);
			SOAPHeaderElement toElement = header.addHeaderElement(name);
			toElement.setValue(to.getContent());
		}
		
		EndpointReference endpointReference = addressing.getEndpointReference();
		if(endpointReference!=null){
			envelope.addNamespaceDeclaration("sonshou", endpointReference.getNamespace());
			Iterator itr = endpointReference.iterator();
			while(itr.hasNext()){
				ReferenceProperty rp = (ReferenceProperty)itr.next();
				Name name = envelope.createName(rp.getKey(), "sonshou", endpointReference.getNamespace());
				SOAPHeaderElement element = header.addHeaderElement(name);
				element.setValue(rp.getValue());
			}
		}
	}
	
	/**
	 * 构造SOAPBody
	 * @param operation
	 * @throws Exception
	 */
	protected abstract void createSoapBodyContent(SOAPEnvelope envelope, JWSDLOperation operation, Namespace[] namespaces) throws Exception;

    /**
     * 处理返回结果
     * @param returnMessage
     * @param outputParam
     * @param clazz
     * @return
     * @throws ResponseException
     */
	public Object handleSOAPResponse(SOAPMessage returnMessage, 
			JWSDLParam outputParam, Class clazz) throws ResponseException
	{
		try{
			SOAPBody body = returnMessage.getSOAPBody();
            return this.processReturnMessageBody(body, outputParam, clazz);
		}catch(Exception e){
			throw new ResponseException(e);
		}
	}
	
	/**
	 * 处理返回的Body中的内容
	 * @param body
	 * @param outputParam
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	protected abstract Object processReturnMessageBody(SOAPBody body, JWSDLParam outputParam, Class clazz) throws ResponseException;
	
	/**
	 * 为复杂类型绑定数据到SOAP中
	 * @param envelope 用来产生节点用,不参与逻辑的
	 * @param element 复杂类型的节点名
	 * @param elName 生成的节点
	 * @param jwsdlType 复杂类型难过
	 * @param value 复杂类型对应的Object值
	 * @param namespaceMap 命名空间集合
	 * @param bindSchemaType 是否绑定参数的类型描述
	 */
	protected void processComplexType(SOAPEnvelope envelope, Map namespaceMap, SOAPElement element,
			QName elName, JWSDLParamType jwsdlType, Object value, boolean bindSchemaType) throws Exception
	{
		if(value==null){
			return;
		}
		
		if(jwsdlType.isArray()){
			if(logger.isDebugEnabled()) logger.debug("绑定数组类型数值: 本地数据- " + value + "  wsdl - " + jwsdlType);
            if(jwsdlType.isComplexTypeArray()){
                //sonshou @see SoapHelper.isParamTypeMatch
                String local = elName.getLocalPart();
                String uri = elName.getNamespaceURI();
                String prefix = (String)namespaceMap.get(uri);
                Name soapName = envelope.createName(local, prefix, uri);
                element = element.addChildElement(soapName);
                elName = jwsdlType.getComplexTypeArrayFieldElementName();
            }

			JWSDLParamType arrayElementType = jwsdlType.getArrayElementType(); //WSDL中的数组元素的类型
			if(value.getClass().isArray()){
				Object[] arrayValues = (Object[])value; //将数组的每个值取出来
                for (Object arrayValue : arrayValues) {
                    processComplexType(envelope, namespaceMap, element, elName,
                            arrayElementType, arrayValue, bindSchemaType);
                }
			}
			
		}else{
			String local = elName.getLocalPart();
			String uri = elName.getNamespaceURI();
			String prefix = (String)namespaceMap.get(uri);
			Name soapName = envelope.createName(local, prefix, uri);
            SOAPElement paramlement = element.addChildElement(soapName);
			
			if(jwsdlType.getQname()!=null&&jwsdlType.getQname().getLocalPart().equalsIgnoreCase("anyType")){
				if(value instanceof Element){
					Node node = paramlement.getOwnerDocument().importNode((Element)value, true);
					paramlement.appendChild(node);
				}else{
					paramlement.setValue(value.toString());
				}
			}else if(jwsdlType.isPrimitive()){
				/*绑定普通类型*/
				if(logger.isDebugEnabled()) logger.debug("绑定基本类型数值: 本地数据- " + value + "  wsdl - " + jwsdlType);
				if(PrimitiveTypeUtil.isMapping(jwsdlType, value.getClass())){
					String valueString = value.toString();
					if(value instanceof Date
							&& PrimitiveTypeUtil.isMapping(jwsdlType, Date.class)){
						String dateString = PrimitiveTypeUtil.dateFormatMapping(jwsdlType);
						valueString = new SimpleDateFormat(dateString).format((Date)value);
					}
					paramlement.setValue(valueString);
				}		
			}else{
				if(value!=null){
					JWSDLParamTypeField[] jwsdlFields = jwsdlType.getDeclaredFields();//复杂类型的字段
					Field[] apiFields = value.getClass().getDeclaredFields();//绑定的对象的字段
					if(jwsdlFields!=null&&apiFields!=null){
						List<JWSDLParamTypeField> alreadyBuild = new ArrayList<>();

                        List<Field> apiFieldList = new ArrayList<>(Arrays.asList(apiFields));
                        for (JWSDLParamTypeField temp : jwsdlFields) {
                            if (!alreadyBuild.contains(temp)) {

                                if (temp.getType().getJavaType().equals(Constant.Choice.class)) {
                                    JWSDLParamTypeChoiceField choiceField = (JWSDLParamTypeChoiceField) temp;
                                    List<JWSDLParamTypeField> choiceList = choiceField.getChoices();
                                    for (JWSDLParamTypeField jwsdlParamTypeField : choiceList) {
                                        for (Iterator<Field> apiFieldItr = apiFieldList.iterator();
                                             apiFieldItr.hasNext(); ) {
                                            Field apifield = apiFieldItr.next();
                                            apifield.setAccessible(true);
                                            Object fieldCallValue = apifield.get(value);
                                            QName jwsdlFieldName = jwsdlParamTypeField.getQname();
                                            String apiName = apifield.getName();

                                            if (jwsdlFieldName.getLocalPart().equalsIgnoreCase(apiName)) {
                                                processComplexType(envelope, namespaceMap, paramlement, jwsdlParamTypeField.getQname(),
                                                        jwsdlParamTypeField.getType(), fieldCallValue, bindSchemaType);
                                                apiFieldItr.remove();
                                                break;
                                            }
                                        }
                                    }
                                } else {
                                    for (Iterator<Field> apiFieldItr = apiFieldList.iterator();
                                         apiFieldItr.hasNext(); ) {
                                        Field apifield = apiFieldItr.next();
                                        apifield.setAccessible(true);
                                        Object fieldCallValue = apifield.get(value);
                                        QName jwsdlFieldName = temp.getQname();
                                        String apiName = apifield.getName();

                                        if (jwsdlFieldName.getLocalPart().equalsIgnoreCase(apiName)) {
                                            processComplexType(envelope, namespaceMap, paramlement, temp.getQname(),
                                                    temp.getType(), fieldCallValue, bindSchemaType);
                                            apiFieldItr.remove();
                                            break;
                                        }
                                    }
                                }
                                alreadyBuild.add(temp);
                            }
                        }
					}
				}
			}
			
			/*如果需要绑定类型,则绑定WSDL中的类型*/
			if(bindSchemaType){
				QName typeName = jwsdlType.getQname();
				if(typeName!=null){
					String typeLocalPart = typeName.getLocalPart();
					String typeURI = typeName.getNamespaceURI();
					String typePrefix = (String)namespaceMap.get(typeURI);
					String typeValueInSOAP = typePrefix+":"+typeLocalPart;
					
					/*http://www.w3.org/2001/XMLSchema-instance*/
					String XSIPrefix = (String)namespaceMap.get(W3CDataTypeConstant.SCHEMA_INTANCE);
					Name soapAttrName = envelope.createName("type", XSIPrefix, W3CDataTypeConstant.SCHEMA_INTANCE);
					paramlement.addAttribute(soapAttrName, typeValueInSOAP);
				}
			}
		}	
	}
	
	/**
	 * 从一个Element中获取一个对象类型数据.用来处理SOAP的返回
	 * @param type
	 * @param clazz
	 * @param w3cElement
	 * @return
	 * @throws Exception
	 */
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

            List<Field> classFields = new ArrayList<Field>(Arrays.asList(clazz.getDeclaredFields()));
			/*设置对象属性的值*/
            for (JWSDLParamTypeField jwsdlField : jwsdlFields) {
                QName jwsdlFieldQName = jwsdlField.getQname();//取得WSDL对象字段描述
                if (jwsdlField.getType().getJavaType().equals(Constant.Choice.class)) {
                    JWSDLParamTypeChoiceField choiceField = (JWSDLParamTypeChoiceField) jwsdlField;
                    List<JWSDLParamTypeField> choices = choiceField.getChoices();
                    for (JWSDLParamTypeField choice : choices) {
                        for (Iterator<Field> itr = classFields.iterator(); itr.hasNext(); ) {
                            Field apiField = itr.next();
                            if (choice.getQname().getLocalPart().equalsIgnoreCase(apiField.getName())) {
                                Object fieldValue = null;

                                NodeList nodeList = w3cElement.getElementsByTagNameNS(choice.getQname().getNamespaceURI(),
                                        choice.getQname().getLocalPart());
                                if (nodeList == null || nodeList.getLength() < 1) {
                                    nodeList = w3cElement.getElementsByTagName(choice.getQname().getLocalPart());
                                }
                                if (nodeList != null && nodeList.getLength() > 0) {
                                    List<Element> elements = new ArrayList<>();
                                    for (int m = 0; m < nodeList.getLength(); m++) {
                                        Element element = (Element) nodeList.item(m);
                                        if (element.getParentNode().isSameNode(w3cElement)) {
                                            elements.add(element);
                                        }
                                    }
                                    if (elements.size() > 0) {
                                        if (choice.getType().isArray()) {
                                            fieldValue = treasArrayType(choice.getType(),
                                                    apiField.getType(), elements);
                                        } else {
                                            fieldValue = treasComplexObject(choice.getType(),
                                                    apiField.getType(), elements.get(0));
                                        }
                                    }
                                }
                                apiField.setAccessible(true);
                                apiField.set(returnObject, fieldValue);
                                itr.remove();
                                break;
                            }
                        }
                    }
                } else {
                    for (Iterator<Field> itr = classFields.iterator(); itr.hasNext(); ) {
                        Field apiField = itr.next();
                        if (logger.isDebugEnabled()) logger.debug(
                                "处理返回结果, 字段:" + apiField.getName()
                        );

                        if (jwsdlFieldQName.getLocalPart().equalsIgnoreCase(apiField.getName())) {
                            Object fieldValue = null;

                            NodeList nodeList = w3cElement.getElementsByTagNameNS(jwsdlFieldQName.getNamespaceURI(),
                                    jwsdlFieldQName.getLocalPart());
                            if (nodeList == null || nodeList.getLength() < 1) {
                                nodeList = w3cElement.getElementsByTagName(jwsdlFieldQName.getLocalPart());
                            }
                            if (nodeList != null && nodeList.getLength() > 0) {
                                List<Element> elements = new ArrayList<>();
                                for (int m = 0; m < nodeList.getLength(); m++) {
                                    Element element = (Element) nodeList.item(m);
                                    if (element.getParentNode().isSameNode(w3cElement)) {
                                        elements.add(element);
                                    }
                                }
                                if (elements.size() > 0) {
                                    if (jwsdlField.getType().isArray()) {
                                        fieldValue = treasArrayType(jwsdlField.getType(),
                                                apiField.getType(), elements);
                                    } else {
                                        fieldValue = treasComplexObject(jwsdlField.getType(),
                                                apiField.getType(), elements.get(0));
                                    }
                                }
                            }
                            apiField.setAccessible(true);
                            apiField.set(returnObject, fieldValue);
                            itr.remove();
                            break;
                        }
                    }
                }
            }
			return returnObject;
		}
	}
	
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
}
