package cn.sonshou.wsinvoker.lang2.wsa.vo;

import javax.xml.namespace.QName;

import cn.sonshou.wsinvoker.lang2.wsa.WSAConstant;

public class WSAAction
{
	private QName messageID = new QName(WSAConstant.namespace, WSAConstant.WSA_ACTION);
	private String content;
	public WSAAction(){}
	public WSAAction(String content){
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
