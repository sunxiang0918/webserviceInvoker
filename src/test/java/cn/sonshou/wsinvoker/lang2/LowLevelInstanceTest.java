package cn.sonshou.wsinvoker.lang2;

import org.junit.Test;

import cn.sonshou.wsinvoker.lang2.soapmessage.OutputObject;
import cn.sonshou.wsinvoker.lang2.w3c.bindingType.ABindingType;
import cn.sonshou.wsinvoker.lang2.wsa.vo.WSAAction;
import cn.sonshou.wsinvoker.lang2.wsa.vo.WSAAddress;
import cn.sonshou.wsinvoker.lang2.wsa.vo.WSAMessageID;
import cn.sonshou.wsinvoker.lang2.wsa.vo.WSARelatesTo;
import cn.sonshou.wsinvoker.lang2.wsa.vo.WSAReplyTo;
import cn.sonshou.wsinvoker.lang2.wsa.vo.WSAddressing;

/**
 * @author SUN
 * @version 1.0
 * @Date 15/9/1 22:55
 */
public class LowLevelInstanceTest {

    @Test
    public void testGetSupportCity() throws Exception {
        
        //测试调用获取支持城市的WS
        OutputObject o = SoapCaller.getInstance("http://www.webxml.com.cn/WebServices/WeatherWebService.asmx?wsdl", ABindingType.SOAP12)
                .setOperatioName("getSupportCity")
                .setParams(new Object[]{"ALL"})
                .invoke();

        System.out.println(o.getSoapReturnMessage());
    }

    @Test
    public void testGetWeatherbyCityName() throws Exception {

        OutputObject o = SoapCaller.getInstance("http://www.webxml.com.cn/WebServices/WeatherWebService.asmx?wsdl", ABindingType.SOAP12)
                .setOperatioName("getWeatherbyCityName")
                .setParams(new Object[]{"三亚"})
                .invoke();

        System.out.println(o.getSoapReturnMessage());
    }

    @Test
    public void testGetCountryCityByIp() throws Exception {

        OutputObject o = SoapCaller.getInstance("http://www.webxml.com.cn/WebServices/IpAddressSearchWebService.asmx?wsdl", ABindingType.SOAP12)
                .setOperatioName("getCountryCityByIp")
                .setParams(new Object[]{"222.209.124.165"})
                .invoke();

        System.out.println(o.getSoapReturnMessage());
    }

    @Test
    public void testFlowInvoker() throws Exception {
        
        //测试了返回对象的直接转换
        OutputObject o = SoapCaller.getInstance("http://10.0.0.7:8080/FlowInvoker?wsdl", ABindingType.SOAP12)
                .setOperatioName("invoker")
                .setExceptReturnType(Integer.class)
                .setParams(new Object[]{"<?xml version=\"1.0\" encoding=\"UTF-8\"?><soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><soap:Body><getCountryCityByIpResponse xmlns=\"http://WebXml.com.cn/\"><getCountryCityByIpResult><string>222.209.124.165</string><string>四川省成都市 电信ADSL</string></getCountryCityByIpResult></getCountryCityByIpResponse></soap:Body></soap:Envelope>\n"})
                .invoke();

        System.out.println(o.getBodyObject());
    }

    @Test
    public void testStartTask() throws Exception {

        //实例化WS消息头
        WSAddressing wsAddressing = new WSAddressing();
        wsAddressing.setMessageID(new WSAMessageID("1231231234123"));
        wsAddressing.setRelatesTo(new WSARelatesTo("badfadfdf1e"));
        wsAddressing.setReplyTo(new WSAReplyTo(new WSAAddress("http://10.0.0.2:8080/taskCallBack?wsdl")));
        wsAddressing.setAction(new WSAAction("callback"));
        wsAddressing.getEndpointReference().addProperty("ReplayToMethod","callback");
        
        //测试了返回对象的直接转换
        OutputObject o = SoapCaller.getInstance("http://10.0.0.7:8080/StartTask?wsdl", ABindingType.SOAP12)
                .setOperatioName("start")
                .setExceptReturnType(Boolean.class)
                .setWSAddressing(wsAddressing)
                .setParams(new Object[]{"<?xml version=\"1.0\" encoding=\"UTF-8\"?><soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><soap:Body><getCountryCityByIpResponse xmlns=\"http://WebXml.com.cn/\"><getCountryCityByIpResult><string>222.209.124.165</string><string>四川省成都市 电信ADSL</string></getCountryCityByIpResult></getCountryCityByIpResponse></soap:Body></soap:Envelope>\n"})
                .invoke();

        System.out.println(o.getBodyObject());
    }
}