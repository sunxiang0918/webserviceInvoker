package cn.sonshou.wsinvoker.lang2.parse.vo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

import cn.sonshou.wsinvoker.lang2.w3c.W3CDataTypeConstant;

/**
 * W3C基本类型封装工厂
 * @author sonshou
 */
public class JWSDLPrimitiveParamTypeFactory 
{
	private static Map<String,JWSDLParamType> cache = new HashMap<>();
	
	/*2013年04月21日20:12:34 sonshou  修改了这个地方.
	 * 现在是要添加两份 QName.因为AXIS1.3 有个bug.有些情况下 它会生成SOAPENV_EncodingStyle11类型的基本类型.
	 * https://jira.atlassian.com/browse/JRA-20351  所以两种QName都要适配 */
	private static String[] qnameCache=new String[]{W3CDataTypeConstant.SCHEMA_LOCATION,W3CDataTypeConstant.SOAPENV_EncodingStyle11};
	
	public static JWSDLParamType getPrimaryType(QName qname)
	{
		synchronized(JWSDLPrimitiveParamTypeFactory.class){
			if(cache.size()<1){
				QName typeName;
				
				for (String qnameLocal : qnameCache) {
					typeName = new QName(qnameLocal,"string");
					JWSDLParamType stringType = new JWSDLParamType(typeName, true, false, String.class, null);
					cache.put(typeName.toString(), stringType);
				}
				
				for (String qnameLocal : qnameCache) {
					typeName = new QName(qnameLocal,"decimal");
					JWSDLParamType decimalType = new JWSDLParamType(typeName, true, false, Number.class, null);
					cache.put(typeName.toString(), decimalType);
				}
				
				for (String qnameLocal : qnameCache) {
					typeName = new QName(qnameLocal,"int");
					JWSDLParamType intType = new JWSDLParamType(typeName, true, false, int.class, null);
					cache.put(typeName.toString(), intType);
				}
				
				for (String qnameLocal : qnameCache) {
					typeName = new QName(qnameLocal,"unsignedInt");
					JWSDLParamType unsignedIntType = new JWSDLParamType(typeName, true, false, int.class, null);
					cache.put(typeName.toString(), unsignedIntType);
				}
				
				for (String qnameLocal : qnameCache) {
					typeName = new QName(qnameLocal,"long");
					JWSDLParamType longType = new JWSDLParamType(typeName, true, false, long.class, null);
					cache.put(typeName.toString(), longType);
				}
				
				for (String qnameLocal : qnameCache) {
					typeName = new QName(qnameLocal,"short");
					JWSDLParamType shortType = new JWSDLParamType(typeName, true, false, short.class, null);
					cache.put(typeName.toString(), shortType);
				}
				
				for (String qnameLocal : qnameCache) {
					typeName = new QName(qnameLocal,"boolean");
					JWSDLParamType booleanType = new JWSDLParamType(typeName, true, false, boolean.class, null);
					cache.put(typeName.toString(), booleanType);
				}
				
				for (String qnameLocal : qnameCache) {
					typeName = new QName(qnameLocal,"double");
					JWSDLParamType doubleType = new JWSDLParamType(typeName, true, false, double.class, null);
					cache.put(typeName.toString(), doubleType);
				}
				
				for (String qnameLocal : qnameCache) {
					typeName = new QName(qnameLocal,"float");
					JWSDLParamType floatType = new JWSDLParamType(typeName, true, false, float.class, null);
					cache.put(typeName.toString(), floatType);
				}
				
				for (String qnameLocal : qnameCache) {
					typeName = new QName(qnameLocal,"date");
					JWSDLParamType dateType = new JWSDLParamType(typeName, true, false, Date.class, null);
					cache.put(typeName.toString(), dateType);
				}
				
				for (String qnameLocal : qnameCache) {
					typeName = new QName(qnameLocal,"dateTime");
					JWSDLParamType datetimeType = new JWSDLParamType(typeName, true, false, Date.class, null);
					cache.put(typeName.toString(), datetimeType);
				}
				
				for (String qnameLocal : qnameCache) {
					typeName = new QName(qnameLocal,"time");
					JWSDLParamType timeType = new JWSDLParamType(typeName, true, false, Date.class, null);
					cache.put(typeName.toString(), timeType);
				}
				
				for (String qnameLocal : qnameCache) {
					typeName = new QName(qnameLocal,"integer");
					JWSDLParamType integerType = new JWSDLParamType(typeName, true, false, Integer.class, null);
					cache.put(typeName.toString(), integerType);
				}
				
				for (String qnameLocal : qnameCache) {
					typeName = new QName(qnameLocal,"anyType");
					JWSDLParamType anyTypeType = new JWSDLParamType(typeName, true, false, Object.class, null);
					cache.put(typeName.toString(), anyTypeType);
				}
				
				for (String qnameLocal : qnameCache) {
					typeName = new QName(qnameLocal,"base64Binary");
					JWSDLParamType base64BinaryType = new JWSDLParamType(typeName, true, false, byte[].class, null);
					cache.put(typeName.toString(), base64BinaryType);
				}
				
				typeName = new QName("localhost","void");
				JWSDLParamType voidType = new JWSDLParamType(typeName, true, false, void.class, null);
				cache.put(typeName.toString(), voidType);
			}
		}
		String key = qname.toString();
		return cache.get(key);
	}
	
	public static JWSDLParamType getPrimaryType(String localName)
	{
		QName qname = new QName(W3CDataTypeConstant.SCHEMA_LOCATION, localName);
		return getPrimaryType(qname);
	}
}
