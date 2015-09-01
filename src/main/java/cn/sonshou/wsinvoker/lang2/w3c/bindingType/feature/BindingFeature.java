package cn.sonshou.wsinvoker.lang2.w3c.bindingType.feature;

import java.util.HashMap;
import java.util.Map;

public class BindingFeature 
{
	private String style;
	private String use;
	public String getStyle() {return style;}
	public void setStyle(String style) {this.style = style;}
	public String getUse() {return use;}
	public void setUse(String use) {this.use = use;}
	
	public BindingFeature(){}
	public BindingFeature(String style, String use){
		this.style = style;
		this.use = use;
	}

	public String toString() {
		return this.style+"/"+this.use;
	}
	public boolean equals(Object obj) {
		if (obj==null) {
			return false;
		}
		
		return this.toString().equalsIgnoreCase(obj.toString());
	}
	
	public static BindingFeature HTTP_GET = new BindingFeature("http", "get");
	public static BindingFeature HTTP_POST = new BindingFeature("http", "post");
	public static BindingFeature DOCUMENT_LITERAL = new BindingFeature("document", "literal");
	public static BindingFeature DOCUMENT_LITERAL_WRAPPED = new BindingFeature("document", "literal/wrapped");
	public static BindingFeature RPC_ENCODED = new BindingFeature("rpc", "encoded");
	public static BindingFeature RPC_LITERAL = new BindingFeature("rpc", "literal");
	
	private static Map<String,BindingFeature> map = new HashMap<>();
	static{
		map.put(HTTP_GET.toString(), HTTP_GET);
		map.put(HTTP_POST.toString(), HTTP_POST);
		map.put(DOCUMENT_LITERAL.toString(), DOCUMENT_LITERAL);
		map.put(RPC_ENCODED.toString(), RPC_ENCODED);
		map.put(RPC_LITERAL.toString(), RPC_LITERAL);
		map.put(DOCUMENT_LITERAL_WRAPPED.toString(), DOCUMENT_LITERAL_WRAPPED);
	}
	public static BindingFeature getByString(String str){
		return map.get(str);
	}
}
