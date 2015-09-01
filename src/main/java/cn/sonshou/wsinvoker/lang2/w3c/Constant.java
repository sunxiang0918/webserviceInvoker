package cn.sonshou.wsinvoker.lang2.w3c;

import javax.xml.namespace.QName;

public interface Constant
{	
	public static String OPERATION_STYLE_REQUESTRESPONSE = "REQUEST_RESPONSE";
	public static String OPERATION_STYLE_ONEWAY = "ONE_WAY";

    public static QName choiceFieldQname = new QName("http://www.w3.org/2001/XMLSchema", "choice");

    public static final class Choice{}
}
