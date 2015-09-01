package cn.sonshou.wsinvoker.lang2.soapmessage;

/**
 * 对应WSDL中的任意类型
 * @author sonshou
 */
public class AnyTypeObject
{
	private String name;
	private String value;
	private AnyTypeObject[] child;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public AnyTypeObject[] getChild() {
		return child;
	}
	public void setChild(AnyTypeObject[] child) {
		this.child = child;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
}
