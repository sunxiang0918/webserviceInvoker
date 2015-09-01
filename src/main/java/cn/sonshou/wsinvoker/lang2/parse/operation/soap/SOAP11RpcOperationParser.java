package cn.sonshou.wsinvoker.lang2.parse.operation.soap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Types;
import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaComplexContent;
import org.apache.ws.commons.schema.XmlSchemaComplexContentRestriction;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;

import cn.sonshou.wsinvoker.lang2.parse.operation.AOperationParser;
import cn.sonshou.wsinvoker.lang2.parse.operation.util.OrderParamUtil;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLOperation;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParam;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParamType;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParamTypeField;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLPrimitiveParamTypeFactory;
import cn.sonshou.wsinvoker.lang2.util.WSInvokerException;
import cn.sonshou.wsinvoker.lang2.w3c.bindingType.ABindingType;
import cn.sonshou.wsinvoker.lang2.w3c.bindingType.feature.BindingFeature;

/**
 * RPC方式的解析处理,将WSDL中的方法解析为本地类型
 * @author sonshou
 */
public class SOAP11RpcOperationParser extends AOperationParser
{
    private static Logger logger = LoggerFactory.getLogger(SOAP11RpcOperationParser.class);
	private static Map<String, XmlSchema[]> schemaCache = new HashMap();
	
	public SOAP11RpcOperationParser(String wsdl) {
		super(wsdl);
	}
	
	private XmlSchema[] schemas;
	private QName operationName;

	public BindingFeature getFeature() {
		return BindingFeature.RPC_ENCODED;
	}
	public ABindingType getType() {
		return ABindingType.SOAP11;
	}
	
	/**
	 * 构造方法描述
	 */
	public JWSDLOperation getJWSDLOperation(Operation operation, Types types, String namespaceURI) throws WSInvokerException {
		String operationName = operation.getName();
		
		QName operationQname = new QName(namespaceURI, operationName);//构造方法名为Qname方式
		if(logger.isDebugEnabled()) logger.debug("方法解析, 方法名:"+operationQname);
		this.operationName = operationQname;
		
		/*取得入参和出参描述*/
		Input input = operation.getInput();//获取Input
		Output output = operation.getOutput(); //获取Output
		
		/*通过Apache的XmlSchemaAPI解析Schema*/
		if(types!=null){
			schemas = schemaCache.get(this.wsdl);
			if(schemas==null||schemas.length<1){
				schemas = this.parseTypesToSchema(types);
				schemaCache.put(this.wsdl, schemas);
			}
		}
		
		Message inputMessage = input.getMessage();
		Message outputMessage = output==null?null:output.getMessage();	
		
		/*处理入参*/
		JWSDLParam[] inputParams = this.parseRPCMessage(inputMessage);
		if(logger.isDebugEnabled()) logger.debug("==================");
		JWSDLParam outputParam = null;
		JWSDLParam[] params = this.parseRPCMessage(outputMessage);
		if(params!=null&&params.length>0){
			outputParam = params[0];
		}
		
		JWSDLOperation jwsdlOperation = new JWSDLOperation();
		jwsdlOperation.setFeature(this.getFeature());
		jwsdlOperation.setLocalName(operationQname.getLocalPart());
		jwsdlOperation.setNamespace(operationQname.getNamespaceURI());
		
		List paramOreder = inputMessage.getOrderedParts(operation.getParameterOrdering());
		if(paramOreder!=null&&paramOreder.size()>0){
			String[] orderList = new String[paramOreder.size()];
			for(int i=0; i<paramOreder.size(); i++){
				Part part = (Part)paramOreder.get(i);
				if(part!=null){
					orderList[i] = part.getName();
				}
			}
			OrderParamUtil.orderParams(inputParams, orderList);
		}
		jwsdlOperation.setInputParam(inputParams);
		jwsdlOperation.setOutputParam(outputParam);
		jwsdlOperation.setAvailable(true);
		jwsdlOperation.setStyle(operation.getStyle().toString());
		return jwsdlOperation;
	}
	
	/**
	 * 解析入参,rpc的part就是入参,且描述了类型,简单类型直接描述,复杂的引用Schema中的元素.
	 * 数组的处理很特殊,完全有别于document/literal,是一种硬编码的风格
	 * @param message
	 * @return
	 * @throws Exception
	 */
	private JWSDLParam[] parseRPCMessage(Message message) throws WSInvokerException
	{
		Map parts = message.getParts();
		if(parts==null||parts.size()<1){
			return null;
		}
		
		/*logit*/
		if(logger.isDebugEnabled()) logger.debug(operationName.toString());
		if(logger.isDebugEnabled()) logger.debug("|__" + operationName + "[" + message.getQName() + "]__" + parts);
		/*开始从Schema中获取参数描述,首先处理入参,对于rpc的,有几个parts则有即个入参*/
		JWSDLParam[] param = new JWSDLParam[parts.size()];
		Iterator itr = parts.values().iterator();
		int count = 0;
		while(itr.hasNext()){
			Part part = (Part)itr.next();
			String paramName = part.getName();
			param[count] = new JWSDLParam(); //创建一个参数描述
			if(logger.isDebugEnabled()) logger.debug("|__" + operationName + "[参数名]__" + paramName);
			QName rpcParamTypeName = part.getTypeName();
			if(rpcParamTypeName==null){
				rpcParamTypeName = part.getElementName();
			}
			JWSDLParamType paramType = this.parseRPCTypeTOParamType(rpcParamTypeName);
			param[count].setType(paramType);
			param[count].setParamName(new QName("",paramName));
			count++;
		}
		return param;
	}
	
	/**
	 * 将RPC的WSDL中的message对应的part中的参数类型,解析为本地类型
	 * @param typeName
	 * @return
	 * @throws Exception
	 */
	private JWSDLParamType parseRPCTypeTOParamType(QName typeName) throws WSInvokerException
	{
		JWSDLParamType paramType = JWSDLPrimitiveParamTypeFactory.getPrimaryType(typeName);
		if(paramType!=null){
			if(logger.isDebugEnabled()) logger.debug("|__" + operationName + "[" + typeName + ",简单类型]");
			return paramType;
		}else if(schemas!=null){
			/*复杂类型,需要从Schema中取得*/
			paramType = new JWSDLParamType();
			paramType.setPrimitive(false);
			XmlSchemaType type = this.findTypeInSchema(typeName, schemas);
				
			/*复杂类型的处理,解析内部的字段,还需要判断是否是数组*/
			if(type instanceof XmlSchemaComplexType){
				XmlSchemaComplexType complexType = (XmlSchemaComplexType)type;
				paramType.setQname(type.getQName());
				if(complexType.getParticle()!=null){
					if(logger.isDebugEnabled()) logger.debug("|__" + operationName + "[" + type.getName() + ",复杂类型]");
					/*对于RPC,这表示它不是数组*/
					XmlSchemaParticle particle = complexType.getParticle();
					if(particle instanceof XmlSchemaSequence){
						/*开始遍历子元素*/
						XmlSchemaSequence sequence = (XmlSchemaSequence)particle;
						XmlSchemaObjectCollection collection = sequence.getItems();
						
						JWSDLParamTypeField[] fields = new JWSDLParamTypeField[collection.getCount()];	
						for(int i=0; i<collection.getCount(); i++){
							fields[i] = new JWSDLParamTypeField();
							XmlSchemaElement element = (XmlSchemaElement)collection.getItem(i);
							if(logger.isDebugEnabled()) logger.debug("|__" + operationName + "[" + complexType.getQName() + "]__复杂元素子元素{" + element.getQName().getNamespaceURI() + "}" + element.getQName().getLocalPart() + "    min" + element.getMinOccurs() + "  max:" + element.getMaxOccurs());
							QName fieldName = element.getQName();

							fields[i].setQname(fieldName);
							
							QName elementTypeName = element.getSchemaTypeName();
							XmlSchemaType elementType = this.findTypeInSchema(elementTypeName, schemas);
							QName arrayElementTypeQName = this.returnArrayElementQNameIfArrayType(elementType);
							if(arrayElementTypeQName!=null){
								/*子元素是数组类型的,处理数组元素*/
								XmlSchemaComplexContent content = (XmlSchemaComplexContent)((XmlSchemaComplexType)elementType).getContentModel();
								XmlSchemaComplexContentRestriction restriction = (XmlSchemaComplexContentRestriction)content.getContent();
								/*对于rpc的,数组类型的类型名称都为{soapenv}:Array.*/
								JWSDLParamType arrayElementJWSDLType = new JWSDLParamType();
								arrayElementJWSDLType.setQname(restriction.getBaseTypeName());
								arrayElementJWSDLType.setArray(true);
								arrayElementJWSDLType.setJavaType(Object[].class);
								if(arrayElementTypeQName.equals(complexType.getQName())){
									/*元素的类型和父类型一样,设置为嵌套类型*/
                                    arrayElementJWSDLType.setArrayElementType(paramType);
								}else{
									/*解析子元素*/
									JWSDLParamType fieldType = parseRPCTypeTOParamType(arrayElementTypeQName);
                                    arrayElementJWSDLType.setArrayElementType(fieldType);
								}
								fields[i].setType(arrayElementJWSDLType);
							}else{
								if(element.getSchemaTypeName().equals(complexType.getQName())){
									/*元素的类型和父类型一样,设置为嵌套类型*/
									fields[i].setType(paramType);
								}else{
									/*解析子元素*/
									JWSDLParamType fieldType = parseRPCTypeTOParamType(element.getSchemaTypeName());
									fields[i].setType(fieldType);
								}
							}
						}
						paramType.setDeclaredFields(fields);
					}
				}
			}else{
				throw new WSInvokerException("part引用未知类型的对象" + typeName);
			}
		}
		return paramType;
	}
	
	/**
	 * 如果是数组,则返回数组的元素类型
	 * @param type QName
	 * 如果不是数组元素,则返回空
	 * @return QName
	 */
	private QName returnArrayElementQNameIfArrayType(XmlSchemaType type) throws WSInvokerException{
		QName arrayElementTypeQName = null;	
		if(!(type instanceof XmlSchemaComplexType)){
			return null;
		}
		XmlSchemaComplexType complexType = (XmlSchemaComplexType)type;
		if(complexType.getContentModel()!=null){
			/*数组*/
			XmlSchemaComplexContent content = (XmlSchemaComplexContent)complexType.getContentModel();
			XmlSchemaComplexContentRestriction restriction = (XmlSchemaComplexContentRestriction)content.getContent();
			/*开始从数组类型定义中获取元素的类型定义*/
			XmlSchemaObjectCollection restrictionCollection = restriction.getAttributes();
			Iterator itr = restrictionCollection.getIterator();
			if(itr.hasNext()){
				XmlSchemaAttribute schemaAttribute = (XmlSchemaAttribute)itr.next();
				Attr[] attrs = schemaAttribute.getUnhandledAttributes();
				if(attrs==null){
					throw new WSInvokerException(
                            "无法取得RPC的WSDL中数组类型" + complexType.getName() + "的attribute定义"
					);
				}
				String typeClassName = "";
				String typeClassNameNamespace = "";
                for (Attr attr : attrs) {
                    if (attr.getNodeName().equalsIgnoreCase("wsdl:arrayType")) {
                        typeClassName = attr.getValue().replaceAll(".*\\:", "")
                                .replaceAll("\\[\\]", "");
                    } else {
                        typeClassNameNamespace = attr.getValue();
                    }
                }
				arrayElementTypeQName = new QName(typeClassNameNamespace, typeClassName);
			}
		}
		return arrayElementTypeQName;
	}
}
