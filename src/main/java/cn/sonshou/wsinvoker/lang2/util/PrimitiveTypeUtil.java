package cn.sonshou.wsinvoker.lang2.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParamType;

/**
 * 判断基础类型和JAVA普通类型(包含Date,String)的匹配
 * 这里之所以不用工具类来处理日期,一是因为工具类中没有这种复杂处理.二是不和过多的包进行依赖.
 * 三是W3c中的日期处理,和一般的不太一样
 * @author sonshou
 */
public class PrimitiveTypeUtil
{
	private static String[] dateFormatStrArray = new String[]{"yyyy.MM.dd G 'at' HH:mm:ss z","EEE, MMM d, ''yy",
		"h:mm a","hh 'o''clock' a, zzzz","K:mm a, z","yyyyy.MMMMM.dd GGG hh:mm aaa",
		"EEE, d MMM yyyy HH:mm:ss Z","yyMMddHHmmssZ","yyyy-MM-dd'T'HH:mm:ss.SSSZ",
		"yyyy-MM-dd HH:mm:ss","yyyy-MM-dd"};
	
	public static boolean isMapping(JWSDLParamType type, Class clazz)
	{
		if(type.isPrimitive()&&clazz.isPrimitive()){
			return type.getQname().getLocalPart().equalsIgnoreCase(clazz.getSimpleName());
		}
		if(type.isPrimitive()&&String.class.isAssignableFrom(clazz)){
			return type.getQname().getLocalPart().equalsIgnoreCase(clazz.getSimpleName());
		}
		if(type.isPrimitive()&&Number.class.isAssignableFrom(clazz)){
			return type.getQname().getLocalPart().equalsIgnoreCase("short")
					||type.getQname().getLocalPart().equalsIgnoreCase("int")
					||type.getQname().getLocalPart().equalsIgnoreCase("integer")
					||type.getQname().getLocalPart().equalsIgnoreCase("long")
					|| type.getQname().getLocalPart().equalsIgnoreCase("double")
					||type.getQname().getLocalPart().equalsIgnoreCase("float")
					||type.getQname().getLocalPart().equalsIgnoreCase("decimal")
					||type.getQname().getLocalPart().equalsIgnoreCase("byte")
					||type.getQname().getLocalPart().equalsIgnoreCase("unsignedInt");
		}
		if(Date.class.isAssignableFrom(clazz)){
			return type.getQname().getLocalPart().equalsIgnoreCase("date")
					||type.getQname().getLocalPart().equalsIgnoreCase("dateTime")
					||type.getQname().getLocalPart().equalsIgnoreCase("time");
		}
		if(Boolean.class.isAssignableFrom(clazz)){
			return type.getQname().getLocalPart().equalsIgnoreCase("boolean");
		}
		if(byte[].class.isAssignableFrom(clazz)||Byte[].class.isAssignableFrom(clazz)){
			return type.getQname().getLocalPart().equalsIgnoreCase("base64Binary")
					||type.getQname().getLocalPart().equalsIgnoreCase("hexBinary");
		}
		return false;
	}

    public static boolean isPrimitive(Class clazz){
        return clazz.isPrimitive()
                ||String.class.isAssignableFrom(clazz)
                ||Number.class.isAssignableFrom(clazz)
                ||Date.class.isAssignableFrom(clazz)
                ||Boolean.class.isAssignableFrom(clazz)
                ||Byte[].class.isAssignableFrom(clazz);
    }
	
	public static String dateFormatMapping(JWSDLParamType type)
	{
		if(type.getQname().getLocalPart().equalsIgnoreCase("date")){
			return "yyyy-MM-dd";
		}else if(type.getQname().getLocalPart().equalsIgnoreCase("dateTime")){
			return "yyyy-MM-dd'T'HH:mm:ss";
		}else if(type.getQname().getLocalPart().equalsIgnoreCase("time")){
			return "HH:mm:ss";
		}else{
			return "";
		}
	}
	
	public static Object createPrimitiveObject(Class clazz, String value) throws WSInvokerException
	{
		if(Integer.class.isAssignableFrom(clazz)||int.class.isAssignableFrom(clazz)){
			return (value==null||value.length()<1)?new Integer(-10001):new Integer(value);
		}
		if(Float.class.isAssignableFrom(clazz)||float.class.isAssignableFrom(clazz)){
			return (value==null||value.length()<1)?new Float(-10001):new Float(value);
		}
		if(Double.class.isAssignableFrom(clazz)||double.class.isAssignableFrom(clazz)){
			return (value==null||value.length()<1)?new Double(-10001):new Double(value);
		}
		if(Long.class.isAssignableFrom(clazz)||long.class.isAssignableFrom(clazz)){
			return (value==null||value.length()<1)?new Long(-10001):new Long(value);
		}
		if(String.class.isAssignableFrom(clazz)){
			return value;
		}
		if(Boolean.class.isAssignableFrom(clazz)||boolean.class.isAssignableFrom(clazz)){
			return (value==null||value.length()<1)? Boolean.FALSE : Boolean.valueOf(value);
		}
		if(byte[].class.isAssignableFrom(clazz)||Byte[].class.isAssignableFrom(clazz)){
			return value.getBytes();
		}
		if(Date.class.isAssignableFrom(clazz)){
			Date date;
            for (String aDateFormatStrArray : dateFormatStrArray) {
                try {
                    date = new SimpleDateFormat(aDateFormatStrArray).parse(value);
                    if (date != null) {
                        return date;
                    }
                } catch (Exception ignored) {
                }
            }
			return null;
		}		
		return null;
	}
}
