package cn.sonshou.wsinvoker.lang2.parse.vo;

import javax.xml.namespace.QName;

/**
 * 参数类型(Class)的元素(Field)描述,类似java.lang.reflect.Field
 * name(字段名)
 * namespace(字段的前缀)
 * fieldClass(字段类型)
 * @author sonshou
 */
public class JWSDLParamTypeField 
{
	private QName qname;
	private JWSDLParamType type;
	
	public QName getQname() {
		return qname;
	}
	public void setQname(QName qname) {
		this.qname = qname;
	}
	public JWSDLParamType getType() {
		return type;
	}
	public void setType(JWSDLParamType type) {
		this.type = type;
	}
	
	public String toString() {
		return (this.qname==null?" ":this.qname.toString()+" ")+type.toString();
	}
	
	public boolean equals(Object obj) {
        return obj != null && this.toString().equalsIgnoreCase(obj.toString());
    }
}
