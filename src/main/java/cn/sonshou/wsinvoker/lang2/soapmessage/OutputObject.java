package cn.sonshou.wsinvoker.lang2.soapmessage;

import cn.sonshou.wsinvoker.lang2.wsa.vo.WSAddressing;

/**
 * WSDLOutput的基类,目前只支持了WS-Address
 * @author sonshou
 */
public class OutputObject
{
	private Object bodyObject;
	private StringBuilder soapReturnMessage;
	private WSAddressing addressing;
	public WSAddressing getAddressing() {
		return addressing;
	}
	public void setAddressing(WSAddressing addressing) {
		this.addressing = addressing;
	}
	public Object getBodyObject() {
		return bodyObject;
	}
	public void setBodyObject(Object bodyObject) {
		this.bodyObject = bodyObject;
	}
	public void setSoapReturnMessage(StringBuilder soapReturnMessage) {
		this.soapReturnMessage = soapReturnMessage;
	}
	public StringBuilder getSoapReturnMessage() {
		return soapReturnMessage;
	}
}
