package cn.sonshou.wsinvoker.lang2.w3c.bindingType;

public abstract class ABindingType 
{
	protected String type;
	protected abstract void setType();
	public String toString() {
		this.setType();
		return type;
	}
	public boolean equals(Object obj) {
		
		if (obj==null) {
			return false;
		}
		
		return this.toString().equalsIgnoreCase(obj.toString());
	}
	
	public static ABindingType HTTP = new HTTP();
	public static ABindingType SOAP11 = new SOAP11();
	public static ABindingType SOAP12 = new SOAP12();
}
