package cn.sonshou.wsinvoker.lang2.wsa.vo;

import javax.xml.namespace.QName;

import cn.sonshou.wsinvoker.lang2.wsa.WSAConstant;

public class WSARelatesTo {
	private QName messageID = new QName(WSAConstant.namespace, WSAConstant.WSA_RELATESTO);
	private String content;
	public WSARelatesTo(){}
	public WSARelatesTo(String content){
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
