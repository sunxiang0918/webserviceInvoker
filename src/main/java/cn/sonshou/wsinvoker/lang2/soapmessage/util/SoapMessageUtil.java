package cn.sonshou.wsinvoker.lang2.soapmessage.util;

import java.io.ByteArrayOutputStream;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

public class SoapMessageUtil
{
	public static String getSoapMessageString(SOAPMessage message) throws Exception
	{
		Source source = message.getSOAPPart().getContent();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		StreamResult streamResult = new StreamResult(outStream);
		transformer.transform(source,streamResult);
		String returnValue = outStream.toString("utf-8");
		outStream.close();
		return returnValue;
	}
}
