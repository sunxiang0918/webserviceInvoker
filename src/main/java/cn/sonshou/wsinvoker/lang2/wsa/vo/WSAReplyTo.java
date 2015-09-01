package cn.sonshou.wsinvoker.lang2.wsa.vo;

import javax.xml.namespace.QName;

import cn.sonshou.wsinvoker.lang2.wsa.WSAConstant;

public class WSAReplyTo
{
	private QName messageID = new QName(WSAConstant.namespace, WSAConstant.WSA_REPLYTO);
	private WSAAddress address;
	public WSAReplyTo(){}
	public WSAReplyTo(WSAAddress address){
		this.address = address;
	}
	
	public QName getMessageID() {
		return messageID;
	}
	public WSAAddress getAddress() {
		return address;
	}
	public void setAddress(WSAAddress address) {
		this.address = address;
	}
}
