package cn.sonshou.wsinvoker.lang2.parse.operation;

import cn.sonshou.wsinvoker.lang2.parse.operation.soap.SOAP11DocumentOperationParser;
import cn.sonshou.wsinvoker.lang2.parse.operation.soap.SOAP11RpcOperationParser;
import cn.sonshou.wsinvoker.lang2.parse.operation.soap.SOAP11RPCLiteralOperationParser;
import cn.sonshou.wsinvoker.lang2.w3c.bindingType.ABindingType;
import cn.sonshou.wsinvoker.lang2.w3c.bindingType.feature.BindingFeature;

/**
 * 获取方法解析类的工厂.
 * 传入Type和bingdingFeture获取
 * 获取的时候,是通过类似"document/literalSOAP11"这种来取得
 * 因为在AOperationParser中设置了toString为这种形式
 * @author sonshou
 */
public class JWSDLSingletonOperationParserFactory 
{
	public static AOperationParser getOperationParser(String wsdl, ABindingType type, BindingFeature feature){
		String key = feature.toString()+type.toString();
		if(key.equalsIgnoreCase(BindingFeature.DOCUMENT_LITERAL.toString()+ABindingType.SOAP11.toString())){
			/*soap11 documentliteral*/
			return new SOAP11DocumentOperationParser(wsdl); 
		}
		if(key.equalsIgnoreCase(BindingFeature.RPC_ENCODED.toString()+ABindingType.SOAP11.toString())){
			/*soap11 rpcencoded*/
			return new SOAP11RpcOperationParser(wsdl); 
		}
		if(key.equalsIgnoreCase(BindingFeature.RPC_LITERAL.toString()+ABindingType.SOAP11.toString())){
			/*soap11 rpcliteral*/
			return new SOAP11RPCLiteralOperationParser(wsdl); 
		}
		if(key.equalsIgnoreCase(BindingFeature.DOCUMENT_LITERAL.toString()+ABindingType.SOAP12.toString())){
			/*soap12 documentliteral*/
			return new SOAP11DocumentOperationParser(wsdl); 
		}
		if(key.equalsIgnoreCase(BindingFeature.RPC_LITERAL.toString()+ABindingType.SOAP12.toString())){
			/*soap12 rpcliteral*/
			return new SOAP11RPCLiteralOperationParser(wsdl); 
		}
		if(key.equalsIgnoreCase(BindingFeature.RPC_ENCODED.toString()+ABindingType.SOAP12.toString())){
			/*soap12 rpcencoded*/
			return new SOAP11RpcOperationParser(wsdl); 
		}
		return null;
	}
}
