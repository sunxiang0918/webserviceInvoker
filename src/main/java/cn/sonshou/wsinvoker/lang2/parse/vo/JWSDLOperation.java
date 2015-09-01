package cn.sonshou.wsinvoker.lang2.parse.vo;

import javax.xml.namespace.QName;

import cn.sonshou.wsinvoker.lang2.w3c.bindingType.feature.BindingFeature;

public class JWSDLOperation
{
	private String invokeTarget;
	private String localName;
	private String namespace;
	private String style; //REQUEST_RESPONSE;ONEWAY,NOTIFYCATION,SOLICIT_RESPONSE,可以根据这个来决定是否等待返回
	private BindingFeature feature; //document/literal, http/post/verb? , http/get/verb?
	private JWSDLParam[] inputParam; //入参
	private JWSDLParam outputParam; //返回
	private QName inputWrapper; //入参的Wrapper,只在document/literal wrapper的时候用
	private String soapActionURI;
	private String faultCode;
	private boolean available;
	public boolean isAvailable() {
		return available;
	}
	public void setAvailable(boolean available) {
		this.available = available;
	}
	public String getFaultCode() {
		return faultCode;
	}
	public void setFaultCode(String faultCode) {
		this.faultCode = faultCode;
	}
	public JWSDLParam[] getInputParam() {
		return inputParam;
	}
	public void setInputParam(JWSDLParam[] inputParam) {
		this.inputParam = inputParam;
	}
	public JWSDLParam getOutputParam() {
		return outputParam;
	}
	public void setOutputParam(JWSDLParam outputParam) {
		this.outputParam = outputParam;
	}
	public String getLocalName() {
		return localName;
	}
	public void setLocalName(String localName) {
		this.localName = localName;
	}
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public String getStyle() {
		return style;
	}
	public void setStyle(String style) {
		this.style = style;
	}
	public BindingFeature getFeature() {
		return feature;
	}
	public void setFeature(BindingFeature feature) {
		this.feature = feature;
	}
	public String getInvokeTarget() {
		return invokeTarget;
	}
	public void setInvokeTarget(String invokeTarget) {
		this.invokeTarget = invokeTarget;
	}
	public String getSoapActionURI() {
		return soapActionURI;
	}
	public void setSoapActionURI(String soapActionURI) {
		this.soapActionURI = soapActionURI;
	}
	public QName getInputWrapper() {
		return inputWrapper;
	}
	public void setInputWrapper(QName inputWrapper) {
		this.inputWrapper = inputWrapper;
	}	
	
	/**
	 * 克隆,主要是把参数清空
	 */
	@Override
	public JWSDLOperation clone() throws CloneNotSupportedException {
		JWSDLOperation obj = new JWSDLOperation();
		obj.setInvokeTarget(this.getInvokeTarget());
		obj.setLocalName(this.getLocalName());
		obj.setNamespace(this.getNamespace());
		obj.setStyle(this.getStyle());
		obj.setFeature(this.getFeature());
		JWSDLParam[] inputParam = this.getInputParam();
		JWSDLParam[] newInputParam = null;
		if(inputParam!=null){
			newInputParam = new JWSDLParam[inputParam.length];
			for(int i=0; i<inputParam.length; i++){
				newInputParam[i] = new JWSDLParam();
				newInputParam[i].setParamName(inputParam[i].getParamName());
				newInputParam[i].setType(inputParam[i].getType());
				newInputParam[i].setValue(null);
			}
		}
		obj.setInputParam(newInputParam);
		
		JWSDLParam outputParam = this.getOutputParam();
		JWSDLParam newOutputParam = null;
		if(outputParam!=null){
			newOutputParam = new JWSDLParam();
			newOutputParam.setParamName(outputParam.getParamName());
			newOutputParam.setType(outputParam.getType());
			newOutputParam.setValue(null);
		}
		obj.setOutputParam(newOutputParam);
		obj.setInputWrapper(this.getInputWrapper());
		obj.setSoapActionURI(this.getSoapActionURI());
		obj.setFaultCode(this.getFaultCode());
		obj.setAvailable(this.isAvailable());
		return obj;
	}
}
