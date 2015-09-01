package cn.sonshou.wsinvoker.lang2.parse;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.wsdl.extensions.soap12.SOAP12Body;
import javax.wsdl.extensions.soap12.SOAP12Operation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.sonshou.wsinvoker.lang2.parse.operation.JWSDLSingletonOperationParserFactory;
import cn.sonshou.wsinvoker.lang2.parse.vo.JWSDLOperation;
import cn.sonshou.wsinvoker.lang2.parse.operation.AOperationParser;
import cn.sonshou.wsinvoker.lang2.parse.operation.JWSDLOperationCache;
import cn.sonshou.wsinvoker.lang2.util.WSInvokerException;
import cn.sonshou.wsinvoker.lang2.w3c.bindingType.ABindingType;
import cn.sonshou.wsinvoker.lang2.w3c.bindingType.feature.BindingFeature;

/**
 * WSDL解析器
 */
public class WSDLParser
{
    private static Logger logger = LoggerFactory.getLogger(WSDLParser.class);
	public JWSDLOperation[] parse(String wsdl, ABindingType type) throws Exception
	{
		if(null==type){
			type = ABindingType.SOAP11;
		}
		
		Definition definition = JWSDLOperationCache.getCachedOperation(wsdl);
		try{
			if(definition==null){
				WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
				reader.setFeature("javax.wsdl.verbose", true);
				reader.setFeature("javax.wsdl.importDocuments", true);
				definition = reader.readWSDL(wsdl);
				JWSDLOperationCache.setToCache(wsdl, definition);
			}	
		}catch(Exception e){
			java.net.URL url = new java.net.URL(wsdl);
			java.io.InputStream stream = url.openStream();
			org.xml.sax.InputSource is = new org.xml.sax.InputSource(stream);
			WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
			reader.setFeature("javax.wsdl.verbose", true);
			reader.setFeature("javax.wsdl.importDocuments", true);
			definition = reader.readWSDL(null, is);
		}

		Types types = definition.getTypes();	
		if(types==null||types.getExtensibilityElements().size()<1){
			Map imports = definition.getImports();
            for (Object o : imports.values()) {
                Vector importVector = (Vector) o;
                for (Object anImportVector : importVector) {
                    Import wsdlImport = (Import) anImportVector;
                    Definition importDefinition = wsdlImport.getDefinition();
                    types = importDefinition.getTypes();
                    if (types != null) {
                        break;
                    }
                }
            }
		}

		if(logger.isDebugEnabled()) logger.debug("wsdl=" + wsdl + "  开始分析binding..");

		JWSDLOperation[] jwsdlOperation = null;
		/*解析Service节点,service节点中endpoint描述:*/
		Map services = definition.getServices();
		Iterator serviceItr = services.keySet().iterator();
		Service service = (Service)services.get(serviceItr.next());
		Map ports = service.getPorts();
        for (Object o1 : ports.keySet()) {
            Port port = (Port) ports.get(o1);
            /*这里面有Service的Location描述*/
            Object serviceLocationAddressInfo = port.getExtensibilityElements().get(0);
            Binding binding = port.getBinding();
            List extElementList = binding.getExtensibilityElements(); //bingding的版本描述,http或soap11,soap12

            /**
             * 这个版本我们只支持SOAP1.1,1.2Binding,对于HTTP暂不支持
             */
            if (type.equals(ABindingType.HTTP)) {
                throw new WSInvokerException("暂不支持HTTP方式");
            }

            PortType portType = binding.getPortType();
            String namespace = portType.getQName().getNamespaceURI();
			/*WSDL:Operation*/
            List bindingOperations = binding.getBindingOperations();

            if (logger.isDebugEnabled()) logger.debug("|__开始构造方法描述..");
            if (jwsdlOperation == null) {
                jwsdlOperation = new JWSDLOperation[bindingOperations.size()];
            }
            Iterator optItr = bindingOperations.iterator();
            int i = 0;
            while (optItr.hasNext()) {
                BindingOperation bindingOperation = (BindingOperation) optItr.next();
                BindingInput bindintInput = bindingOperation.getBindingInput();

                BindingFeature feature = null;
				/*endpoint暂时用wsdl来标识*/
                String invokeTarget = wsdl.replaceAll("\\?wsdl", "");
				/*SOAP的soapAction*/
                String soapActionURI = "";
				/*判断WSDL中是否有当前传入的标准的Binding,目前就是SOAP11*/
                if (type.equals(ABindingType.SOAP11)) {
                    Object extention = extElementList.get(0);
                    if (extention.toString().contains(type.toString())) {
                        SOAPBody soapBody = (SOAPBody) bindintInput.getExtensibilityElements().get(0);
                        String use = soapBody.getUse();
                        SOAPOperation soapOpertaionBody = (SOAPOperation) bindingOperation.getExtensibilityElements().get(0);
                        String style = soapOpertaionBody.getStyle();
                        if (style == null) {
                            SOAPBinding soapBindg = (SOAPBinding) binding.getExtensibilityElements().get(0);
                            style = soapBindg.getStyle();
                        }
                        String action = soapOpertaionBody.getSoapActionURI();
                        if (action != null) {
                            soapActionURI = action;
                        }
                        feature = new BindingFeature(style, use);
                        SOAPAddress soapAddress = (SOAPAddress) serviceLocationAddressInfo;
                        invokeTarget = soapAddress.getLocationURI();
                    }
                } else if (type.equals(ABindingType.SOAP12)) {
                    for (Object anExtElementList : extElementList) {
                        if (anExtElementList instanceof ExtensibilityElement) {
                            /*SOAP12无法识别为对象,因此先取得的是binding的一个element.*/
                            ExtensibilityElement bindingExt = (ExtensibilityElement) anExtElementList;
							/*如果这个element是soap12的命名空间,则是soap12*/
                            if (bindingExt.getElementType().getNamespaceURI().equals(type.toString())) {
                                List extensiElementList = bindintInput.getExtensibilityElements();
                                String use = null;
                                for (Object anExtensiElementList : extensiElementList) {
                                    if (anExtensiElementList instanceof SOAP12Body) {
                                        SOAP12Body soap12Body = (SOAP12Body) anExtensiElementList;
                                        use = soap12Body.getUse();
                                        break;
                                    } else {
                                        /*无法识别为SoapBody*/
                                        UnknownExtensibilityElement element = (UnknownExtensibilityElement) anExtensiElementList;
                                        QName typeName = element.getElementType();
                                        if (typeName.getLocalPart().equalsIgnoreCase("body")) {
                                            use = element.getElement().getAttribute("use");
                                            break;
                                        }
                                    }
                                }
                                if (use == null) {
                                    throw new WSInvokerException("WSDL:" + wsdl + " 中binding的Input没有soapbody");
                                }

                                String style;
                                if (bindingOperation.getExtensibilityElements().get(0) instanceof SOAP12Operation) {
                                    SOAP12Operation soapOpertaionBody = (SOAP12Operation) bindingOperation.getExtensibilityElements().get(0);
                                    style = soapOpertaionBody.getStyle();
                                    if (style == null) {
                                        SOAP12Binding soapBindg = (SOAP12Binding) binding.getExtensibilityElements().get(0);
                                        style = soapBindg.getStyle();
                                    }
                                    String action = soapOpertaionBody.getSoapActionURI();
                                    if (action != null) {
                                        soapActionURI = action;
                                    }
                                } else {
                                    UnknownExtensibilityElement soap12Element = (UnknownExtensibilityElement) bindingOperation.getExtensibilityElements().get(0);
								/*无法识别为SoapBind*/
                                    style = soap12Element.getElement().getAttribute("style");
                                    if (style == null) {
									    /*TODO: 可能有错*/
                                        SOAPBinding soapBindg = (SOAPBinding) binding.getExtensibilityElements().get(0);
                                        style = soapBindg.getStyle();
                                    }
                                    String action = soap12Element.getElement().getAttribute("soapAction");
                                    if (action != null) {
                                        soapActionURI = action;
                                    }
                                }

                                feature = new BindingFeature(style, use);
                                List elements = port.getExtensibilityElements();
                                for (Object o : elements) {
                                    if (o instanceof SOAP12Address) {
                                        SOAP12Address address = (SOAP12Address) o;
                                        invokeTarget = address.getLocationURI();
                                    } else if (o instanceof UnknownExtensibilityElement) {
                                        UnknownExtensibilityElement element = (UnknownExtensibilityElement) o;
                                        QName typeName = element.getElementType();
                                        if (typeName.getLocalPart().equalsIgnoreCase("address")) {
                                            invokeTarget = element.getElement().getAttribute("location");
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (feature != null) {
                    if (logger.isDebugEnabled()) logger.debug("|__通过soapbinding(" + type + ":" + feature + ")获取解析类..");
                    AOperationParser parser = JWSDLSingletonOperationParserFactory.getOperationParser(wsdl, type, feature);
                    if (parser != null) {
                        if (logger.isDebugEnabled()) logger.debug("|__解析类" + parser.getClass() + "..");
                        String faultCode;
                        try {
                            jwsdlOperation[i] = parser.getJWSDLOperation(bindingOperation.getOperation(), types, namespace);
                            jwsdlOperation[i].setInvokeTarget(invokeTarget);
                            jwsdlOperation[i].setSoapActionURI(soapActionURI);
                        } catch (Exception e) {
                            if (logger.isDebugEnabled()) logger.error("解析方法出现错误:", e);
                            faultCode = e.toString();
                            jwsdlOperation[i] = new JWSDLOperation();
                            jwsdlOperation[i].setFaultCode(faultCode);
                            jwsdlOperation[i].setAvailable(false);
                        }
                    }
                }
                i++;
            }
        }	

		int faildOpCount = 0;
        assert jwsdlOperation != null;
        for (JWSDLOperation aJwsdlOperation : jwsdlOperation) {
            if (aJwsdlOperation == null || !aJwsdlOperation.isAvailable()) {
                faildOpCount++;
            }
        }
		
		if(faildOpCount==jwsdlOperation.length){
			throw new WSInvokerException("WSDL:"+wsdl+" 中解析所有方法均出现错误,无可用方法.比如有些元素是引用了schema");
		}
	
		return jwsdlOperation;
	}
}
