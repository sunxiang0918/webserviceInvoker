package cn.sonshou.wsinvoker.lang2.util;

public class ResponseException extends Exception
{
	public ResponseException(String fault){
		super(fault);
	}
	
	public ResponseException(Throwable e){
		super(e);
	}
}
