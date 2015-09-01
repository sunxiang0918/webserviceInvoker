package cn.sonshou.wsinvoker.lang2.parse.operation.soap;

import java.util.ArrayList;
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
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.sonshou.wsinvoker.lang2.parse.operation.AOperationParser;
import cn.sonshou.wsinvoker.lang2.parse.operation.util.OrderParamUtil;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLOperation;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParam;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParamType;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParamTypeChoiceField;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParamTypeField;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLPrimitiveParamTypeFactory;
import cn.sonshou.wsinvoker.lang2.util.WSInvokerException;
import cn.sonshou.wsinvoker.lang2.w3c.Constant;
import cn.sonshou.wsinvoker.lang2.w3c.bindingType.ABindingType;
import cn.sonshou.wsinvoker.lang2.w3c.bindingType.feature.BindingFeature;

/**
 * SOAP document/literal方式的解析处理
 * @author sonshou
 */
public class SOAP11DocumentOperationParser extends AOperationParser
{
    private static Logger logger = LoggerFactory.getLogger(SOAP11DocumentOperationParser.class);
	private static Map<String, XmlSchema[]> schemaCache = new HashMap<>();
	
	public SOAP11DocumentOperationParser(String wsdl) {
		super(wsdl);
	}
	
	private XmlSchema[] schemas;
	private QName operationName;
	protected ABindingType type = ABindingType.SOAP11;
	protected BindingFeature feature = BindingFeature.DOCUMENT_LITERAL;
	
	
	public BindingFeature getFeature() {
		return feature;
	}
	public ABindingType getType() {
		return type;
	}

	public JWSDLOperation getJWSDLOperation(Operation operation, Types types, String namespaceURI) throws WSInvokerException 
	{
		/*通过Apache的XmlSchemaAPI解析Schema*/
		if(types!=null){
			schemas = schemaCache.get(this.wsdl);
			if(schemas==null||schemas.length<1){
				schemas = this.parseTypesToSchema(types);
				schemaCache.put(this.wsdl, schemas);
			}
			if(schemas==null||schemas.length<1){
				throw new WSInvokerException("WSDL没有Schema描述");
			}
		}
		
		String operationName = operation.getName();
		List paramOreder = operation.getParameterOrdering();	
		QName operationQname = new QName(namespaceURI, operationName);//构造方法名为Qname方式
		if(logger.isDebugEnabled()) logger.debug("方法解析, 方法名:" + operationQname);
		this.operationName = operationQname;
		/*取得入参和出参描述*/
		Input input = operation.getInput();//获取Input
		Output output = operation.getOutput(); //获取Output

		JWSDLOperation jwsdlOp;
		try{
			if(logger.isDebugEnabled()) logger.debug("paring document/literal operation : " + operationQname + "........");
			jwsdlOp = this.documentLiteralOperationParse(operationQname, input, output, paramOreder);
		}catch(Exception e){
			if(logger.isDebugEnabled()) logger.error("方法:" + operationQname + "无法使用,因为:",e);
			throw new WSInvokerException("方法:" + operationQname + "无法使用,因为" + e);
		}
		if(jwsdlOp!=null){
			jwsdlOp.setStyle(operation.getStyle().toString());
			jwsdlOp.setAvailable(true);
		}

		return jwsdlOp;
	}
	
	/**
	 * 将WSDL中的方法描述,转换为我们清楚的形式,包含参数描述
	 * Document/Literal(非wrapper的),它的参数是通过Schema中的Element来描述,其中,Message部分的part是引用这些Element
	 * 因此,首先解析Input或OuputMessage中的Parts,通过parts去Schema中找到Element
	 * InputMessage表示入参,入参中的parts个数和顺序,就是服务方法的参数和顺序
	 * @param operationName
	 * @param input
	 * @param output
	 */
	private JWSDLOperation documentLiteralOperationParse(QName operationName, Input input,
					Output output, List paramOreder) throws Exception
	{
		Message inputMessage=null;
		Message outputMessage =null;
		if (input!=null) {
			inputMessage = input.getMessage();
		}
		
		if (output!=null) {
			outputMessage = output.getMessage();	
		}
		
		/*处理入参*/
		JWSDLParam[] inputParams = this.parseMessage(inputMessage);
		if(logger.isDebugEnabled()) logger.debug("==================");
		JWSDLParam outputParam = null;
		JWSDLParam[] params = this.parseMessage(outputMessage);
		if(params!=null&&params.length>0){
			outputParam = params[0];
		}
		
		JWSDLOperation jwsdlOperation = new JWSDLOperation();
		jwsdlOperation.setFeature(BindingFeature.DOCUMENT_LITERAL);
		jwsdlOperation.setLocalName(operationName.getLocalPart());
		jwsdlOperation.setNamespace(operationName.getNamespaceURI());

        assert inputMessage != null;
        List paramOrderParts = inputMessage.getOrderedParts(paramOreder);
		if(paramOrderParts!=null&&paramOrderParts.size()>0){
			String[] orderList = new String[paramOrderParts.size()];
			for(int i=0; i<paramOrderParts.size(); i++){
				Part part = (Part)paramOrderParts.get(i);
				if(part!=null){
					orderList[i] = part.getElementName().getLocalPart();
				}
			}
			OrderParamUtil.orderParams(inputParams, orderList);
		}

        /*2015-06-11 sonshou 将wrapper的解开操作放在解析这里。否则，如果按照【以前的逻辑】：
        * ---SoapHelper中find方法，匹配不到operation的时候，再来解开wrapper尝试查找---
        * 那么，当执行到正常的isParamMatch的时候，会由于TypeCompareUtil新增的对特殊数组的处理逻辑{line：65}
        * 而导致“方法匹配”。
        * 实际上，应该是解开wrapper后匹配。这就导致了错误
        * 典型的情况是：字符串数组作为参数
        * wsdl中，方法名作为顶层元素，其类型描述是一个“包含0-unbound的string”的复杂类型
        * java方法中，传入的是一个字符串数组。
        * 这种情况下，应该是解开wrapper来对比，然而，上述【以前的逻辑】在未解开的时候，由于TypeCompareUtil
        * 的对比情况，导致了一次错误的匹配成功。
        *
        * 解开的逻辑：
        * 当一个元素，是WSDL中Schema的顶层元素，并且被解析为唯一一个入参的时候，且该元素名称就是方法名称，则对其进行解开wrapper的处理
        * */
        if(inputParams!=null&&inputParams.length==1){
            JWSDLParam wrapperParam = inputParams[0];
            //wrapperParam对应到WSDL的话，一定是一个顶层element了，所以这里可以直接判断
            if(wrapperParam.getParamName().getLocalPart().equalsIgnoreCase(operationName.getLocalPart())){
                JWSDLParamTypeField[] fields = wrapperParam.getType().getDeclaredFields();
                if(fields==null){
                    inputParams = null;
                }else {
                    inputParams = new JWSDLParam[fields.length];
                    jwsdlOperation.setInputWrapper(wrapperParam.getParamName());
                    for (int m = 0; m < fields.length; m++) {
                        inputParams[m] = new JWSDLParam();
                        inputParams[m].setParamName(fields[m].getQname());
                        inputParams[m].setType(fields[m].getType());
                    }
                }
            }
        }

		jwsdlOperation.setInputParam(inputParams);
		jwsdlOperation.setOutputParam(outputParam);
		if(outputParam==null){
			jwsdlOperation.setStyle(Constant.OPERATION_STYLE_ONEWAY);
		}
		return jwsdlOperation;
	}
	
	/**
	 * 解析入参,document/literal的part直接指向入参element,因此该element既描述了参数名,又描述了类型
	 * @param message
	 * @return
	 * @throws Exception
	 */
	private JWSDLParam[] parseMessage(Message message) throws WSInvokerException
	{
		if (message==null) {
			return null;
		}
		
		Map parts = message.getParts();
		if(parts==null||parts.size()<1){
			return null;
		}
		/*logit*/
		if(logger.isDebugEnabled()) logger.debug(operationName.toString());
		if(logger.isDebugEnabled()) logger.debug("|__" + operationName + "[" + message.getQName() + "]__" + parts);
		/*开始从Schema中获取参数描述,首先处理入参,对于document/literal,有即个parts则有即个入参*/
		JWSDLParam[] param = new JWSDLParam[parts.size()];
		Iterator itr = parts.values().iterator();
		int count = 0;
		while(itr.hasNext()){
			Part part = (Part)itr.next();
			QName elementQNameInSchema = part.getElementName();
			/*从Schema中获取part对应的Element,准备将其解析为一个入参描述*/
			XmlSchemaElement schemaElement = this.findElementInSchema(elementQNameInSchema, schemas);
			if(schemaElement==null){
				throw new WSInvokerException("方法" + operationName + " 中的part:" + elementQNameInSchema + " 没有document/literal的schemaelment描述,WSDL错误");
			}
			if(logger.isDebugEnabled()) logger.debug("|__" + operationName + "[" + message.getQName() + "]__find element in schema with the parts name: " + elementQNameInSchema);
			if(logger.isDebugEnabled()) logger.debug("|__" + operationName + "[" + message.getQName() + "]__" + schemaElement.getQName());
			
			
			QName paramName = schemaElement.getQName();//元素名即是入参的参数名
			param[count] = new JWSDLParam(); //创建一个参数描述
			param[count].setParamName(paramName);
			/*将这个元素解析为参数的类型描述*/
			/* 获取每个参数的参数名和类型描述,参数就是通过Part取得的Element*/
			/*复杂元素子元素需要处理嵌套的情况*/
			XmlSchemaType type = schemaElement.getSchemaType();
			if(type==null){
				QName typeQName = schemaElement.getSchemaTypeName();
				type = this.findTypeInSchema(typeQName, this.schemas);
			}
			if(type==null){
				throw new WSInvokerException(
                        "Type not found in schema, element=" + schemaElement.getQName());
			}

            //2015-05-06 sonshou 增加一个解析栈。用于嵌套处理
            Map<QName, JWSDLParamType> typeStack = new HashMap<QName, JWSDLParamType>();
			JWSDLParamType paramType = this.parseDcoumentLiteralWSDLTypeToParamType(type, 
					schemaElement.getMinOccurs(), schemaElement.getMaxOccurs(), typeStack);
			if(paramType==null){
				throw new WSInvokerException("方法" + operationName + " 中的part:" + elementQNameInSchema + " 无法解析的参数类型描述(" + paramName + "),WSDL错误");
			}
			param[count].setType(paramType);
			count++;
		}
		return (param.length > 0)?param:null;
	}

	/**
	 * 将document/literal中的入参元素类型解析为参数描述类型
	 * 
	 * 这个方法决定该元素作为参数来说,是数组对象还是非数组对象
	 * @param type
	 * @return
	 * @throws Exception
	 */
	JWSDLParamType parseDcoumentLiteralWSDLTypeToParamType(XmlSchemaType type,
			long minOccors, long maxOccurs,  Map<QName, JWSDLParamType> typeStack) throws WSInvokerException
	{
        if(type.getQName()!=null&&typeStack.containsKey(type.getQName())){
            return typeStack.get(type.getQName());
        }

		JWSDLParamType paramType = new JWSDLParamType();
        String unName = "UnNamed" + System.currentTimeMillis();

		if(type instanceof XmlSchemaComplexType){
			XmlSchemaComplexType complexType = (XmlSchemaComplexType)type;
			if(minOccors>=0&&maxOccurs>1){
				paramType.setArray(true);
                paramType.setJavaType(Object[].class);
                paramType.setPrimitive(false);
				JWSDLParamType arryType = this.recuiseComplexType(complexType, typeStack);
                paramType.setArrayElementType(arryType);
                paramType.setQname(type.getQName()==null?new QName("UnNamed",unName)
                    :new QName(type.getQName().getNamespaceURI(),"_Array_"+type.getQName().getLocalPart()));
			}else{
				paramType = this.recuiseComplexType(complexType, typeStack);
			}
		}else if(type instanceof XmlSchemaSimpleType){
			if(minOccors>=0&&maxOccurs>1){
				paramType.setArray(true);
                paramType.setJavaType(Object[].class);
                paramType.setPrimitive(false);
				JWSDLParamType arryType = JWSDLPrimitiveParamTypeFactory.getPrimaryType(type.getQName());
                paramType.setArrayElementType(arryType);
                paramType.setQname(type.getQName()==null?new QName("UnNamed",unName)
                    :new QName(type.getQName().getNamespaceURI(),"_Array_"+type.getQName().getLocalPart()));
			}else{
				/*简单类型一定会是用type属性引用定义*/
				QName typeName = type.getQName();
				if(typeName==null){
					/*2015-04-27 sonshou 有可能是枚举类型.说不定还有更多类型*/
					Object content = ((XmlSchemaSimpleType) type).getContent();
					if(content!=null&&(content instanceof XmlSchemaSimpleTypeRestriction)){
						typeName = ((XmlSchemaSimpleTypeRestriction)content).getBaseTypeName();
					}else{
						//可能是其他类型
						throw new UnsupportedOperationException("该简单类型暂时未支持解析");
					}
				}
				paramType = JWSDLPrimitiveParamTypeFactory.getPrimaryType(typeName);
				if(logger.isDebugEnabled()) logger.debug("|__" + operationName + "[" + type.getQName() + ",简单类型]_" + paramType);
			}
		}

        if(!typeStack.containsKey(type.getQName())) {
            typeStack.put(paramType.getQname(), paramType);
        }
		return paramType;
	}
	
	/**
	 * 解析复杂类型(类型和元素是有区别的)
	 * @param complexType
	 * @return
	 * @throws Exception
	 */
	private JWSDLParamType recuiseComplexType(XmlSchemaComplexType complexType, Map<QName, JWSDLParamType> typeStack) throws WSInvokerException
	{
        JWSDLParamType jwsdlParamTypes;
        if(complexType.getQName()!=null&&typeStack.containsKey(complexType.getQName())) {
            return typeStack.get(complexType.getQName());
        }

        jwsdlParamTypes = new JWSDLParamType();
        String unName = "UnNamed" + System.currentTimeMillis();
        jwsdlParamTypes.setQname(complexType.getQName()==null?new QName("UnNamed",unName):complexType.getQName());
        typeStack.put(jwsdlParamTypes.getQname(), jwsdlParamTypes);

		if(logger.isDebugEnabled()) logger.debug("|__" + operationName + "[" + complexType.getQName() + "]__解析复杂元素");
		XmlSchemaParticle particle = complexType.getParticle();
		if(particle==null){
			jwsdlParamTypes.setJavaType(Void.class);
            typeStack.put(complexType.getQName(), jwsdlParamTypes);
		}else if(particle instanceof XmlSchemaSequence){
			XmlSchemaSequence sequence = (XmlSchemaSequence)particle;
			XmlSchemaObjectCollection collection = sequence.getItems();

			if(collection.getCount()<1){
				jwsdlParamTypes.setJavaType(Void.class);
				return jwsdlParamTypes;
			}else{
                jwsdlParamTypes.setJavaType(Object.class);
                jwsdlParamTypes.setPrimitive(false);

                JWSDLParamTypeField[] fields = new JWSDLParamTypeField[collection.getCount()];
                for (int i = 0; i < collection.getCount(); i++) {
                    XmlSchemaObject schemaObj = collection.getItem(i);
                    fields[i] = parseElementToField(schemaObj, jwsdlParamTypes, typeStack);
                }
                jwsdlParamTypes.setDeclaredFields(fields);
            }
		}

		return jwsdlParamTypes;
	}
	
	/**
	 * 解析复杂元素子元素.
	 * 参数1表示子元素，参数2，3用于递归
	 * @param schemaObj XmlSchemaObject
	 * @param schemaObjFromParsed JWSDLParamType
     * @param typeStack
	 * @return JWSDLParamTypeField
	 */
	protected JWSDLParamTypeField parseElementToField(XmlSchemaObject schemaObj,
			JWSDLParamType schemaObjFromParsed, Map<QName, JWSDLParamType> typeStack) throws WSInvokerException{
		JWSDLParamTypeField field = new JWSDLParamTypeField();
		if(schemaObj instanceof XmlSchemaElement){
			XmlSchemaElement element = (XmlSchemaElement)schemaObj;
		
			if(element.getRefName()!=null&&
					element.getRefName().getLocalPart().equalsIgnoreCase("schema")){
				throw new WSInvokerException("包含Schema的引用参数,该方法无法支持");
			}
			if(logger.isDebugEnabled()) logger.debug("|__" + operationName + "[" + schemaObjFromParsed.getQname() + "]__复杂元素子元素{" + element.getQName().getNamespaceURI() + "}" + element.getQName().getLocalPart() + "    min" + element.getMinOccurs() + "  max:" + element.getMaxOccurs());
			field.setQname(element.getQName());
			
			/*复杂元素子元素需要处理嵌套的情况*/
			XmlSchemaType type = element.getSchemaType();
			if(type==null){
				QName typeQName = element.getSchemaTypeName();
				type = this.findTypeInSchema(typeQName, this.schemas);
			}
			if(type==null){
				throw new WSInvokerException("Type not found in schema, element="+element.getQName());
			}
			if(type.getQName()!=null&&type.getQName().equals(schemaObjFromParsed.getQname())){
				/*元素的类型和父类型一样,设置为嵌套类型*/
                //嵌套类型本身可能是个数组
                if(element.getMinOccurs()>=0&&element.getMaxOccurs()>1){
                    JWSDLParamType fieldType = new JWSDLParamType();
                    fieldType.setArray(true);
                    fieldType.setJavaType(Object[].class);
                    fieldType.setPrimitive(false);
                    fieldType.setArrayElementType(schemaObjFromParsed);
                    fieldType.setQname(new QName(type.getQName().getNamespaceURI(),"_Array_"+type.getQName().getLocalPart()));
                    field.setType(fieldType);
                }else {
                    field.setType(schemaObjFromParsed);
                }
			}else if(type.getQName()!=null&&typeStack.containsKey(type.getQName())){
                //嵌套类型本身可能是个数组
                JWSDLParamType fieldType = typeStack.get(type.getQName());
                if(element.getMinOccurs()>=0&&element.getMaxOccurs()>1){
                    JWSDLParamType newType = new JWSDLParamType();
                    newType.setArray(true);
                    newType.setJavaType(Object[].class);
                    newType.setPrimitive(false);
                    newType.setArrayElementType(fieldType);
                    newType.setQname(new QName(type.getQName().getNamespaceURI(),"_Array_"+type.getQName().getLocalPart()));
                    field.setType(newType);
                }else {
                    field.setType(fieldType);
                }
            }else{
				/*解析子元素*/
				JWSDLParamType fieldType = this.parseDcoumentLiteralWSDLTypeToParamType(
                    type, element.getMinOccurs(), element.getMaxOccurs(), typeStack);
				field.setType(fieldType);
			}
		}else if(schemaObj instanceof XmlSchemaChoice){
            //Choice类型
			XmlSchemaChoice choiceElement = (XmlSchemaChoice)schemaObj;
			field = new JWSDLParamTypeChoiceField();

			field.setQname(Constant.choiceFieldQname);
            JWSDLParamType fieldType = new JWSDLParamType();
            fieldType.setJavaType(Constant.Choice.class);
            fieldType.setQname(new QName("choice", "choice"+System.currentTimeMillis()));
            field.setType(fieldType);

			if(logger.isDebugEnabled()) logger.debug("|__" + operationName + "[" + schemaObjFromParsed.getQname() + "]__复杂元素子元素是一个choice类型");

			//解析choice的子元素
			XmlSchemaObjectCollection choiceCollections = choiceElement.getItems();
			List<JWSDLParamTypeField> choices = new ArrayList<>();
			if(choiceCollections!=null&&choiceCollections.getCount()>0){
				for(int j=0; j<choiceCollections.getCount(); j++){
					XmlSchemaObject choiceCollectionEl = choiceCollections.getItem(j);
					JWSDLParamTypeField choiceField = parseElementToField(choiceCollectionEl, schemaObjFromParsed, typeStack);
					choices.add(choiceField);
				}
			}
			((JWSDLParamTypeChoiceField)field).setChoices(choices);
		}

		return field;
	}
}
