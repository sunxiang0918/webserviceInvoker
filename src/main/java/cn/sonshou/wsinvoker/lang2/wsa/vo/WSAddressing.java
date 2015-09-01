package cn.sonshou.wsinvoker.lang2.wsa.vo;

import cn.sonshou.wsinvoker.lang2.wsa.endpointreference.EndpointReference;

public class WSAddressing 
{
	private WSAMessageID messageID;
	private WSAReplyTo replyTo;
	private WSATo to;
	private WSAAction action;
	private WSARelatesTo relatesTo;
	private EndpointReference endpointReference;
	
	public EndpointReference getEndpointReference() {
		return endpointReference;
	}
	public void setEndpointReference(EndpointReference endpointReference) {
		this.endpointReference = endpointReference;
	}
	public WSARelatesTo getRelatesTo() {
		return relatesTo;
	}
	public void setRelatesTo(WSARelatesTo relatesTo) {
		this.relatesTo = relatesTo;
	}
	public WSAAction getAction() {
		return action;
	}
	public void setAction(WSAAction action) {
		this.action = action;
	}
	public WSAMessageID getMessageID() {
		return messageID;
	}
	public void setMessageID(WSAMessageID messageID) {
		this.messageID = messageID;
	}
	public WSAReplyTo getReplyTo() {
		return replyTo;
	}
	public void setReplyTo(WSAReplyTo replyTo) {
		this.replyTo = replyTo;
	}
	public WSATo getTo() {
		return to;
	}
	public void setTo(WSATo to) {
		this.to = to;
	}
}
