package cn.sonshou.wsinvoker.lang2.soapmessage;

import java.util.HashMap;
import java.util.Map;

import cn.sonshou.wsinvoker.lang2.soapmessage.soap11.SOAP11RPCEncodeSOAPHandler;
import cn.sonshou.wsinvoker.lang2.soapmessage.soap11.SOAP11RPCLiteralSOAPHandler;
import cn.sonshou.wsinvoker.lang2.w3c.bindingType.feature.BindingFeature;
import cn.sonshou.wsinvoker.lang2.soapmessage.soap11.SOAP11DocumentLiteralSOAPHandler;
import cn.sonshou.wsinvoker.lang2.w3c.bindingType.ABindingType;

public class SOAPHandlerFactory 
{
	private static Map<String,ASOAPHandler> cache = new HashMap<>();
	public static ASOAPHandler getHanlder(ABindingType soapType, BindingFeature feature)
	{
		if(cache==null||cache.size()<1){
			/*SOAP1.1*/
			cache.put(ABindingType.SOAP11+""+BindingFeature.DOCUMENT_LITERAL, new SOAP11DocumentLiteralSOAPHandler(ABindingType.SOAP11));
			cache.put(ABindingType.SOAP11+""+BindingFeature.RPC_ENCODED, new SOAP11RPCEncodeSOAPHandler(ABindingType.SOAP11));
			cache.put(ABindingType.SOAP11+""+BindingFeature.RPC_LITERAL, new SOAP11RPCLiteralSOAPHandler(ABindingType.SOAP11));
			/*SOAP1.2*/
			cache.put(ABindingType.SOAP12+""+BindingFeature.DOCUMENT_LITERAL, new SOAP11DocumentLiteralSOAPHandler(ABindingType.SOAP12));
						cache.put(ABindingType.SOAP12+""+BindingFeature.RPC_ENCODED, new SOAP11RPCEncodeSOAPHandler(ABindingType.SOAP12));
			cache.put(ABindingType.SOAP12+""+BindingFeature.RPC_LITERAL, new SOAP11RPCLiteralSOAPHandler(ABindingType.SOAP12));
		}
		
		return cache.get(soapType+""+feature);
	}
}
