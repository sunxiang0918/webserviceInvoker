package cn.sonshou.wsinvoker.lang2.wsa.vo;

import javax.xml.namespace.QName;

import cn.sonshou.wsinvoker.lang2.wsa.WSAConstant;

public class WSAAddress 
{
	private QName messageID = new QName(WSAConstant.namespace, WSAConstant.WSA_ADDRESS);
	private String content;
	public WSAAddress(){}
	public WSAAddress(String content){
		this.content = content;
	}
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public QName getMessageID() {
		return messageID;
	}
}
