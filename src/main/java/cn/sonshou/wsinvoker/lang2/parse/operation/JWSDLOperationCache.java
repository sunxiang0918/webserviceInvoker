package cn.sonshou.wsinvoker.lang2.parse.operation;

import java.util.HashMap;
import java.util.Map;
import javax.wsdl.Definition;

public class JWSDLOperationCache 
{
	private static Map<String,Definition> cache = new HashMap<>();
	
	public static Definition getCachedOperation(String wsdl)
	{
		return cache.get(wsdl);
	}
	
	public static void setToCache(String wsdl, Definition definition)
	{
		cache.put(wsdl, definition);
	}
	
	public static boolean containsWSDL(String wsdl)
	{
		return cache.containsKey(wsdl);
	}
}
