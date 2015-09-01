package cn.sonshou.wsinvoker.lang2;

import java.util.HashMap;
import java.util.Map;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import cn.sonshou.wsinvoker.lang2.parse.WSDLParser;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLOperation;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLParam;
import cn.sonshou.wsinvoker.lang2.soapmessage.ASOAPHandler;
import cn.sonshou.wsinvoker.lang2.w3c.Constant;
import cn.sonshou.wsinvoker.lang2.wsa.vo.WSAddressing;
import cn.sonshou.wsinvoker.lang2.soapmessage.ISoapReturnMessageAdapter;
import cn.sonshou.wsinvoker.lang2.soapmessage.OutputObject;
import cn.sonshou.wsinvoker.lang2.soapmessage.SOAPHandlerFactory;
import cn.sonshou.wsinvoker.lang2.soapmessage.util.SoapMessageUtil;
import cn.sonshou.wsinvoker.lang2.util.ResponseException;
import cn.sonshou.wsinvoker.lang2.util.WSInvokerException;
import cn.sonshou.wsinvoker.lang2.util.namespace.NameSpaceUtil;
import cn.sonshou.wsinvoker.lang2.util.namespace.Namespace;
import cn.sonshou.wsinvoker.lang2.w3c.bindingType.ABindingType;

/**
 * 构造SOAP协议,发送请求,接收返回数据,分析
 * @author sonshou
 */
public class SoapCaller
{
	private WSAddressing addressing;
	private String operationName;
	private Object[] params;
	private Class exceptReturnType;
	private String username;//还不知道如何实现WS-Secuirity
	private String password;//还不知道如何实现WS-Secuirity
	private JWSDLOperation[] jwsdlOperation;
	private ABindingType bindingType;
	private ISoapReturnMessageAdapter returnMessageAdapter; //自定义处理返回的接口
	
	private static WSDLParser wsdlParser = new WSDLParser();
    private static Logger logger = LoggerFactory.getLogger(SoapCaller.class);
	
	private static Map<String, JWSDLOperation[]> operationCache = new HashMap<String, JWSDLOperation[]>();
	
	private SoapCaller(String wsdl, ABindingType bindingType) throws Exception
	{
		this.bindingType = bindingType;
		String key = wsdl + bindingType.toString();
		JWSDLOperation[] opertaions = operationCache.get(key);
        if(opertaions==null){
            synchronized (SoapCaller.class){
                if(opertaions==null){
                    opertaions = wsdlParser.parse(wsdl, bindingType);
                    operationCache.put(key, opertaions);
                }else{
                    opertaions = operationCache.get(key);
                }
            }
        }

		if(opertaions==null){
			throw new UnsupportedOperationException(wsdl + "中,没有解析出方法");
		}
		int len = opertaions.length;
		this.jwsdlOperation = new JWSDLOperation[len];
		for(int i=0; i<len; i++){
			this.jwsdlOperation[i] = opertaions[i].clone(); 
		}
	}
	
	public static SoapCaller getInstance(String wsdl, ABindingType bindingType) throws Exception
	{
		return new SoapCaller(wsdl, bindingType);
	}
	
	public SoapCaller setOperatioName(String operationName)
	{
		this.operationName = operationName;
		return this;
	}
	
	public SoapCaller setExceptReturnType(Class exceptReturnType)
	{
		this.exceptReturnType = exceptReturnType;
		return this;
	}
	
	public SoapCaller setParams(Object[] params)
	{
		this.params = params;
		return this;
	}
	
	public SoapCaller setWSAddressing(WSAddressing addressing)
	{
		this.addressing = addressing;
		return this;
	}
	
	public SoapCaller setReturnMessageAdapter(ISoapReturnMessageAdapter returnMessageAdapter)
	{
		this.returnMessageAdapter = returnMessageAdapter;
		return this;
	}
	
	public Object invoke() throws Exception
	{
		if(operationName==null){
			throw new WSInvokerException("没有设置方法名");
		}
		
		if(addressing==null){
			if(logger.isDebugEnabled()) logger.debug("!没有设置WS-Addressing信息...");
		}
		if(exceptReturnType==null){
			if(logger.isDebugEnabled()) logger.debug("!没有设置exceptReturnType...");
		}
		
		JWSDLOperation currentOperation = SoapHelper.establishAnalyzeContext(jwsdlOperation)
												    .findMatchedOperation(operationName, params)
												    .bindValue();
		if(currentOperation==null){
			throw new Exception("没有找到方法"+operationName);
		}
		Namespace[] namespaces = NameSpaceUtil.newInstance().analyzeNamespaces(currentOperation, this.bindingType);
		

		ASOAPHandler handler = SOAPHandlerFactory.getHanlder(this.bindingType, currentOperation.getFeature());
		if(addressing!=null){	
			handler.addWSAddressing(addressing);
		}
		SOAPMessage message = handler.createSoapMessage(currentOperation, namespaces);
		message.getMimeHeaders().setHeader("SOAPAction", currentOperation.getSoapActionURI());
		if(logger.isDebugEnabled()){		
			logger.debug("构造出的SOAP请求: " + SoapMessageUtil.getSoapMessageString(message));
		}
		
		System.setProperty("sun.net.client.defaultReadTimeout", "20000");
		SOAPConnectionFactory factory = SOAPConnectionFactory.newInstance();
		SOAPConnection connection = factory.createConnection();
		SOAPMessage returnMessage;
		try{
			returnMessage = connection.call(message, currentOperation.getInvokeTarget());
		}catch(Throwable e){
			connection.close();
			logger.error("错误:",e);
			throw new Exception(e);
		}
		
		connection.close();
		
		if (currentOperation.getStyle().equalsIgnoreCase(Constant.OPERATION_STYLE_ONEWAY)) {
			return null;
		}	
		
		if (returnMessage==null) {
			return null;
		}	

		/*开始处理返回数据*/
		StringBuilder soapReturn = new StringBuilder(SoapMessageUtil.getSoapMessageString(returnMessage));
		if(logger.isDebugEnabled()){
			logger.debug("接收到的SOAP返回:" + soapReturn);
		}
		SOAPPart replySoapPart = returnMessage.getSOAPPart();
        SOAPEnvelope replySoapEnv = replySoapPart.getEnvelope();
        SOAPBody replySoapBody = replySoapEnv.getBody();
        if (replySoapBody.hasFault()) {
        	logger.info("ERROR: " + replySoapBody.getFault().getFaultString());
        	throw new WSInvokerException("WS调用出现错误: faultCode="+replySoapBody.getFault().getFaultCode()+
        			"  faultString="+replySoapBody.getFault().getFaultString());
        }
 
        OutputObject result = new OutputObject();
		/*如果设置了自定义返回处理类,则使用自定义处理来解析返回数据*/
		if(this.returnMessageAdapter!=null){
			if(logger.isDebugEnabled()) logger.debug("自定义处理类:" + this.returnMessageAdapter + "开始处理");
			Object bodyObject = this.returnMessageAdapter.handleSOAPResponse(returnMessage, 
					currentOperation.getOutputParam(), exceptReturnType);
			result.setBodyObject(bodyObject);
		}else
		/*如果设置了返回类型,并且方法实现了处理,则将返回的数据构造成返回的对象,否则直接返回SOAPMessage数据*/
		if(this.exceptReturnType!=null&&!void.class.isAssignableFrom(exceptReturnType)){
			if(logger.isDebugEnabled()) logger.debug("配置了返回类型,进行转换");
			if(currentOperation.getOutputParam()==null){
				if(logger.isDebugEnabled()) logger.debug("配置了返回类型,但WSDL解析没有发现需要返回数据,因此返回空");
				return null;
			}
			Object bodyObject = handler.handleSOAPResponse(returnMessage, 
					currentOperation.getOutputParam(), exceptReturnType);
			result.setBodyObject(bodyObject);
		}		
		result.setSoapReturnMessage(soapReturn);		
		return result;
	}

    /**
     * 这个是直接返回一个底层的调用实例,采用这个方法 不会自动的去解析WSDL.而是需要用户手动传入SOAP的Element.这个方法的作用就是用来处理那些不标准的SOAP.
     * @param wsdl
     * @param bindingType
     * @return
     * @throws Exception
     */
	public static LowLevelInstance getLowLevelInstance(String wsdl, ABindingType bindingType) throws Exception{
		SoapCaller caller = new SoapCaller(wsdl, bindingType);
        return new LowLevelInstance(caller);
	}
	
	public static class LowLevelInstance {
		private SoapCaller caller;
		private WSAddressing addressing;
		private String operationName;
		private Element soapBodyContent;
        protected static Logger logger = LoggerFactory.getLogger(LowLevelInstance.class);

		private LowLevelInstance(SoapCaller caller){
			this.caller = caller;
		}
		
		public LowLevelInstance setOperatioName(String operationName)
		{
			this.operationName = operationName;
			return this;
		}
		
		public LowLevelInstance setParams(Element soapBodyContent)
		{
			this.soapBodyContent = soapBodyContent;
			return this;
		}
		
		public LowLevelInstance setWSAddressing(WSAddressing addressing)
		{
			this.addressing = addressing;
			return this;
		}
		
		public OutputObject invoke() throws Exception{
			if(operationName==null){
				throw new WSInvokerException("没有设置方法名");
			}
			if(addressing==null){
				if(logger.isDebugEnabled()) logger.debug("!没有设置WS-Addressing信息...");
			}
			
			JWSDLOperation currentOperation = null;
			for(JWSDLOperation op : this.caller.jwsdlOperation){
				if(op.isAvailable()&&op.getLocalName().equalsIgnoreCase(operationName)){
					currentOperation = op;
					break;
				}
			}
			
			if(currentOperation==null){
				throw new Exception("没有找到方法"+operationName);
			}
			
			ASOAPHandler handler = new ASOAPHandler(this.caller.bindingType) {			
				@Override
				protected Object processReturnMessageBody(SOAPBody body,
						JWSDLParam outputParam, Class clazz) throws ResponseException {
					return null;
				}	
				@Override
				protected void createSoapBodyContent(SOAPEnvelope envelope,
						JWSDLOperation operation, Namespace[] namespaces) throws Exception {
					SOAPElement body = envelope.getBody();				
					Node node = body.getOwnerDocument()
							.importNode(LowLevelInstance.this.soapBodyContent, true);
					body.appendChild(node);
				}
			};
			if(addressing!=null){	
				handler.addWSAddressing(addressing);
			}
			SOAPMessage message = handler.createSoapMessage(currentOperation, new Namespace[]{});
			message.getMimeHeaders().setHeader("SOAPAction", currentOperation.getSoapActionURI());
			if(logger.isDebugEnabled()){		
				logger.debug("构造出的SOAP请求: " + SoapMessageUtil.getSoapMessageString(message));
			}
			
			System.setProperty("sun.net.client.defaultReadTimeout", "20000");
			SOAPConnectionFactory factory = SOAPConnectionFactory.newInstance();
			SOAPConnection connection = factory.createConnection();
			SOAPMessage returnMessage;
			try{
				returnMessage = connection.call(message, currentOperation.getInvokeTarget());
			}catch(Throwable e){
				connection.close();
				logger.error("错误",e);
				throw new Exception(e);
			}
			
			connection.close();
			
			if (currentOperation.getStyle().equalsIgnoreCase(Constant.OPERATION_STYLE_ONEWAY)) {
				return null;
			}	
			
			if (returnMessage==null) {
				return null;
			}	

			/*开始处理返回数据*/
			StringBuilder soapReturn = new StringBuilder(SoapMessageUtil.getSoapMessageString(returnMessage));
			if(logger.isDebugEnabled()){
				logger.debug("接收到的SOAP返回:" + soapReturn);
			}
			SOAPPart replySoapPart = returnMessage.getSOAPPart();
	        SOAPEnvelope replySoapEnv = replySoapPart.getEnvelope();
	        SOAPBody replySoapBody = replySoapEnv.getBody();
	        if (replySoapBody.hasFault()) {
	        	logger.info("ERROR: " + replySoapBody.getFault().getFaultString());
	        	throw new WSInvokerException("WS调用出现错误: faultCode="+replySoapBody.getFault().getFaultCode()+
	        			"  faultString="+replySoapBody.getFault().getFaultString());
	        }
	        OutputObject result = new OutputObject();
	        
	        result.setBodyObject(returnMessage.getSOAPBody().getFirstChild());
			result.setSoapReturnMessage(soapReturn);		
			return result;
		}
	}
}
