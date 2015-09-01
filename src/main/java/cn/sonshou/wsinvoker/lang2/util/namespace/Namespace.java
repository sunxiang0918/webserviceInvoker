package cn.sonshou.wsinvoker.lang2.util.namespace;

public class Namespace 
{
	private String prefix;
	private String uri;
	public Namespace(){}
	public Namespace(String prefix, String uri){
		this.prefix = prefix;
		this.uri = uri;
	}
	
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	
}
