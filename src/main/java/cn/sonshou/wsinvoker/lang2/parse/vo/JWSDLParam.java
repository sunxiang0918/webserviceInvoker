package cn.sonshou.wsinvoker.lang2.parse.vo;

import javax.xml.namespace.QName;

public class JWSDLParam
{
	private JWSDLParamType type;
	private QName paramName;
	private Object value;
	public QName getParamName() {
		return paramName;
	}
	public void setParamName(QName paramName) {
		this.paramName = paramName;
	}
	public JWSDLParamType getType() {
		return type;
	}
	public void setType(JWSDLParamType type) {
		this.type = type;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}

	public String toString() {
		return "[" + type.toString() + "]@" + paramName;
	}
}
