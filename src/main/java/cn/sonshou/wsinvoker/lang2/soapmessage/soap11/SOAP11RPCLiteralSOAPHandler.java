package cn.sonshou.wsinvoker.lang2.soapmessage.soap11;

import cn.sonshou.wsinvoker.lang2.w3c.bindingType.ABindingType;

public class SOAP11RPCLiteralSOAPHandler extends SOAP11RPCEncodeSOAPHandler
{
	public SOAP11RPCLiteralSOAPHandler(ABindingType bindingType) {
		super(bindingType);
	}
	
	protected boolean isBindType() {
		return false;
	}
}
