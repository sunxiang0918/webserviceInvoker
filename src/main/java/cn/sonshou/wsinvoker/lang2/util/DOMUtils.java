package cn.sonshou.wsinvoker.lang2.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DOMUtils {
    /**
     * 初始化一个空Document对象返回。
     *
     * @return a Document
     */
    public static Document newXMLDocument() {
        try {
            return newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 初始化一个DocumentBuilder
     *
     * @return a DocumentBuilder
     * @throws ParserConfigurationException
     */
    public static DocumentBuilder newDocumentBuilder()
        throws ParserConfigurationException {
        return newDocumentBuilderFactory().newDocumentBuilder();
    }

    /**
     * 初始化一个DocumentBuilderFactory
     *
     * @return a DocumentBuilderFactory
     */
    public static DocumentBuilderFactory newDocumentBuilderFactory() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf;
    }

    /**
     * 将传入的一个XML String转换成一个org.w3c.dom.Document对象返回。
     *
     * @param xmlString
     *            一个符合XML规范的字符串表达。
     * @return a Document
     */
    public static Document parseXMLDocument(String xmlString) {
        if (xmlString == null) {
            throw new IllegalArgumentException();
        }
        try {
            return newDocumentBuilder().parse(
                new InputSource(new StringReader(xmlString)));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 给定一个输入流，解析为一个org.w3c.dom.Document对象返回。
     *
     * @param input
     * @return a org.w3c.dom.Document
     */
    public static Document parseXMLDocument(InputStream input) {
        if (input == null) {
            throw new IllegalArgumentException("参数为null！");
        }
        try {
            return newDocumentBuilder().parse(input);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 给定一个文件名，获取该文件并解析为一个org.w3c.dom.Document对象返回。
     *
     * @param fileName
     *            待解析文件的文件名
     * @return a org.w3c.dom.Document
     */
    public static Document loadXMLDocumentFromFile(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("未指定文件名及其物理路径！");
        }
        try {
            return newDocumentBuilder().parse(new File(fileName));
        } catch (SAXException e) {
            throw new IllegalArgumentException("目标文件（" + fileName
                + "）不能被正确解析为XML！" + e.getMessage());
        } catch (IOException e) {
            throw new IllegalArgumentException("不能获取目标文件（" + fileName + "）！"
                + e.getMessage());
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /*
     * 把dom文件转换为xml字符串
     */
    public static String toStringFromDoc(Document document) {
        String result = null;

        if (document != null) {
            StringWriter strWtr = new StringWriter();
            StreamResult strResult = new StreamResult(strWtr);
            TransformerFactory tfac = TransformerFactory.newInstance();
            try {
                javax.xml.transform.Transformer t = tfac.newTransformer();
                t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                t.setOutputProperty(OutputKeys.INDENT, "yes");
                t.setOutputProperty(OutputKeys.METHOD, "xml"); // xml, html,
                // text
                t.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount", "4");
                t.transform(new DOMSource(document.getDocumentElement()),
                    strResult);
            } catch (Exception e) {
                System.err.println("XML.toString(Document): " + e);
            }
            result = strResult.getWriter().toString();
            try {
                strWtr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 给定一个节点，将该节点加入新构造的Document中。
     *
     * @param node
     *            a Document node
     * @return a new Document
     */

    public static Document newXMLDocument(Node node) {
        Document doc = newXMLDocument();
        doc.appendChild(doc.importNode(node, true));
        return doc;
    }

    /**
     * 获取一个Transformer对象，由于使用时都做相同的初始化，所以提取出来作为公共方法。
     *
     * @return a Transformer encoding gb2312
     */
    public static Transformer newTransformer() {
        try {
            Transformer transformer = TransformerFactory.newInstance()
                .newTransformer();
            Properties properties = transformer.getOutputProperties();
            properties.setProperty(OutputKeys.ENCODING, "gb2312");
            properties.setProperty(OutputKeys.METHOD, "xml");
            properties.setProperty(OutputKeys.VERSION, "1.0");
            properties.setProperty(OutputKeys.INDENT, "no");
            transformer.setOutputProperties(properties);
            return transformer;
        } catch (TransformerConfigurationException tce) {
            throw new RuntimeException(tce.getMessage());
        }
    }

    /**
     * 返回一段XML表述的错误信息。
     *
     * @param title
     *            提示的title
     * @param errMsg
     *            提示错误信息
     * @param errClass
     *            抛出该错误的类，用于提取错误来源信息。
     * @return a XML String show err msg
     */

    public static String errXMLString(String title, String errMsg,
                                      Class errClass) {
        StringBuilder msg = new StringBuilder(100);
        msg.append("<?xml version='1.0' encoding='utf-8' ?>");
        msg.append("<errNode title=").append(title).append("errMsg=").append(errMsg).append("errSource=").append(errClass.getName()).append("/>");
        return msg.toString();
    }

}





