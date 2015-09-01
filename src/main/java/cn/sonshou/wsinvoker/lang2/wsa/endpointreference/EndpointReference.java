package cn.sonshou.wsinvoker.lang2.wsa.endpointreference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * WSA 端点引用
 * @author sonshou
 */
public class EndpointReference {
	private List<ReferenceProperty> referenceProperties = new ArrayList<>();
	private String namespace;
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public EndpointReference(String namespace) throws Exception{
		if(namespace==null||namespace.length()<1){
			throw new Exception("namespace mustn't be null");
		}
		this.namespace = namespace;
	}
	
	public void addProperty(String key, String value) throws Exception{
		if(key==null){
			throw new Exception("key mustn't be null!");
		}
		if(value==null){
			value = "";
		}
		ReferenceProperty property = new ReferenceProperty(key, value);
		referenceProperties.add(property);
	}
	
	public Iterator iterator(){
		return referenceProperties.iterator();
	}
}
