package cn.sonshou.wsinvoker.lang2.parse.vo;

import javax.xml.namespace.QName;

/**
 * WSDL参数类型描述,类似JAVA中反射出来的Class.包含name(类似类名)
 * namespace(命名空间,类似包名)
 * isPrimitive(是否是简单类型-w3c中的基本类型)
 * declaredFields(属性字段).
 * qname参数类型名称,比如 {http://www.w3.org/2001/XMLSchema}:string
 * @author sonshou
 */
public class JWSDLParamType
{
	protected QName qname;
    private boolean isPrimitive = true;
    private boolean isArray;
    private Class javaType;
    private JWSDLParamTypeField[] declaredFields;
    private JWSDLParamType arrayElementType;
    /*
     * 以下代表AAA这个类型中的两个数组定义方式
         *<xs:complexType name="AAA">
         *   <xs:sequence>
         *       <xs:element name="array1" minOccurs="0">
         *           <xs:complexType>
         *               <xs:sequence>
         *                   <xs:element name="array1Element" type="tns:array1Element" minOccurs="0" maxOccurs="unbounded"/>
         *               </xs:sequence>
         *           </xs:complexType>
         *       </xs:element>
         *       <xs:element name="array2Element" type="tns:arrayElement2" minOccurs="0" maxOccurs="unbounded"/>
         *   </xs:sequence>
         * </xs:complexType>
         *
         * 如果采用第一种，则构造的soap数组中有array1这个名称为父节点
         * 否则，没有数组名称父节点
         *
         * 以上两个样例为：
         * <AAA>
         *     <array1>
         *         <array1Element/>
         *         <array1Element/>
         *         <array1Element/>
         *     </array1>
         * </AAA>
         *
         * <AAA>
         *     <array2Element/>
         *     <array2Element/>
         *     <array2Element/>
         * </AAA>
        */
    private boolean isComplexTypeArray; //是否是complex包装的array
    private QName complexTypeArrayFieldElementName;//看图说话

	public String toString() {
		return "Qname:"+qname+
				"  isPrimitive:"+ isPrimitive +
				"  isArray:"+isArray+
				"  javaType:"+javaType;
	}
	
	public JWSDLParamType(){}
	public JWSDLParamType(QName qname, 
						  boolean isPrimitive, 
						  boolean isArray, 
						  Class javaType,
						  JWSDLParamTypeField[] declaredFields){
		this.qname = qname;
		this.isPrimitive = isPrimitive;
		this.isArray = isArray;
		this.javaType = javaType;
		this.declaredFields = declaredFields;
	}
	
	public JWSDLParamTypeField[] getDeclaredFields() {
		return declaredFields;
	}
	public void setDeclaredFields(JWSDLParamTypeField[] declaredFields) {
		this.declaredFields = declaredFields;
	}
	public boolean isPrimitive() {
		return isPrimitive;
	}
	public void setPrimitive(boolean isPrimative) {
		this.isPrimitive = isPrimative;
	}
	public boolean isArray() {
		return isArray;
	}
	public void setArray(boolean isArray) {
		this.isArray = isArray;
	}
	public Class getJavaType() {
		return javaType;
	}
	public void setJavaType(Class javaType) {
		this.javaType = javaType;
	}
	public QName getQname() {
		return qname;
	}
	public void setQname(QName qname) {
		this.qname = qname;
	}

    public void setArrayElementType(JWSDLParamType arrayElementType) {
        this.arrayElementType = arrayElementType;
    }

    public void setComplexTypeArray(boolean isComplexTypeArray) {
        this.isComplexTypeArray = isComplexTypeArray;
    }

    public JWSDLParamType getArrayElementType() {
        return arrayElementType;
    }

    public boolean isComplexTypeArray() {
        return isComplexTypeArray;
    }

    public void setComplexTypeArrayFieldElementName(QName complexTypeArrayFieldElementName) {
        this.complexTypeArrayFieldElementName = complexTypeArrayFieldElementName;
    }

    public QName getComplexTypeArrayFieldElementName() {
        return complexTypeArrayFieldElementName;
    }
}

