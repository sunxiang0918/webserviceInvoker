package cn.sonshou.wsinvoker.lang2.w3c;

public interface W3CDataTypeConstant 
{
	public static String SCHEMA_LOCATION = "http://www.w3.org/2001/XMLSchema";
	public static String SCHEMA_INTANCE = "http://www.w3.org/2001/XMLSchema-instance";
	
	public static String SOAPENV_EncodingStyle11 = "http://schemas.xmlsoap.org/soap/encoding/";
	public static String SOAPENV_EncodingStyle12 = "http://www.w3.org/2003/05/soap-encoding";
	public static String XMLNS_SOAPENV11 = "http://schemas.xmlsoap.org/wsdl/soap/";
	public static String XMLNS_SOAPENV12 = "http://www.w3.org/2003/05/soap-envelope";
	
	public static String W3C_STRING = "{"+SCHEMA_LOCATION+"}"+"string";
	public static String W3C_DECIMAL = "{"+SCHEMA_LOCATION+"}"+"decimal";
	public static String W3C_INT = "{"+SCHEMA_LOCATION+"}"+"int"; //整数
	public static String W3C_LONG = "{"+SCHEMA_LOCATION+"}"+"long"; //64位整数
	public static String W3C_SHORT = "{"+SCHEMA_LOCATION+"}"+"short"; //16位证书
	public static String W3C_BOOLEAN = "{"+SCHEMA_LOCATION+"}"+"boolean";   
	public static String W3C_DOUBLE = "{"+SCHEMA_LOCATION+"}"+"double";   
	public static String W3C_FLOAT = "{"+SCHEMA_LOCATION+"}"+"float"; 
	public static String W3C_DATE = "{"+SCHEMA_LOCATION+"}"+"date"; //YYYY-MM-DD
	public static String W3C_DATETIME = "{"+SCHEMA_LOCATION+"}"+"dateTime"; //YYYY-MM-DDThh:mm:ss
	public static String W3C_TIME = "{"+SCHEMA_LOCATION+"}"+"time"; //hh:mm:ss
	public static String W3C_BYTE = "{"+SCHEMA_LOCATION+"}"+"byte";
	public static String W3C_INTEGER = "{"+SCHEMA_LOCATION+"}"+"integer";
	public static String W3C_ANYTYPE = "{"+SCHEMA_LOCATION+"}"+"anyType";
	public static String W3C_BASE64BINARY =  "{"+SCHEMA_LOCATION+"}"+"base64Binary";
	
	public static String W3C_WSDL_ARRAYTYPEPREFIX = "ArrayOf";
	public static String W3C_RPC_ARRAYELEMENT_BASETYPE_SOAP11 = "{"+SOAPENV_EncodingStyle11+"}Array";
	
	/** 以下是暂时未使用的W3C类型
	"{"+SCHEMA_LOCATION+"}"+ENTITY
	"{"+SCHEMA_LOCATION+"}"+ID
	"{"+SCHEMA_LOCATION+"}"+NCName
	"{"+SCHEMA_LOCATION+"}"+nonPositiveInteger
	"{"+SCHEMA_LOCATION+"}"+ENTITIES
	"{"+SCHEMA_LOCATION+"}"+NMTOKENS
	"{"+SCHEMA_LOCATION+"}"+gYearMonth
	"{"+SCHEMA_LOCATION+"}"+unsignedByte
	"{"+SCHEMA_LOCATION+"}"+nonNegativeInteger
	"{"+SCHEMA_LOCATION+"}"+Name
	"{"+SCHEMA_LOCATION+"}"+NOTATION
	"{"+SCHEMA_LOCATION+"}"+positiveInteger
	"{"+SCHEMA_LOCATION+"}"+duration
	"{"+SCHEMA_LOCATION+"}"+gMonthDay
	"{"+SCHEMA_LOCATION+"}"+token
	"{"+SCHEMA_LOCATION+"}"+negativeInteger
	"{"+SCHEMA_LOCATION+"}"+IDREFS
	"{"+SCHEMA_LOCATION+"}"+normalizedString
	"{"+SCHEMA_LOCATION+"}"+anySimpleType
	"{"+SCHEMA_LOCATION+"}"+IDREF
	"{"+SCHEMA_LOCATION+"}"+anyURI
	"{"+SCHEMA_LOCATION+"}"+unsignedInt
	"{"+SCHEMA_LOCATION+"}"+unsignedShort
	"{"+SCHEMA_LOCATION+"}"+unsignedLong
	"{"+SCHEMA_LOCATION+"}"+QName
	"{"+SCHEMA_LOCATION+"}"+gMonth
	"{"+SCHEMA_LOCATION+"}"+gDay
	"{"+SCHEMA_LOCATION+"}"+hexBinary
	"{"+SCHEMA_LOCATION+"}"+gYear
	"{"+SCHEMA_LOCATION+"}"+language
	"{"+SCHEMA_LOCATION+"}"+NMTOKEN
	*/
}
