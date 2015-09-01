package cn.sonshou.wsinvoker.lang2.parse.operation.soap;

import cn.sonshou.wsinvoker.lang2.w3c.bindingType.feature.BindingFeature;

public class SOAP11RPCLiteralOperationParser extends SOAP11RpcOperationParser
{
	public SOAP11RPCLiteralOperationParser(String wsdl) {
		super(wsdl);
	}

	public BindingFeature getFeature() {
		return BindingFeature.RPC_LITERAL;
	}
}
