package top.cafebabe.easywebdav.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLParser {

    public static List<DavFileFactory> propFindResult(byte[] xmlString) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Node doc = builder.parse(new ByteArrayInputStream(xmlString)).getChildNodes().item(0);
            NodeList nodes = doc.getChildNodes();
            List<DavFileFactory> res = new LinkedList<>();
            for (int i = 0; i < nodes.getLength(); i++) {
                DavFileFactory factory = oneFile(nodes.item(i));
                factory.setExist(true);
                res.add(factory);
            }
            return res;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static DavFileFactory oneFile(Node node) {
        DavFileFactory davFileFactory = metaDate(node.getChildNodes().item(1).getFirstChild());
        davFileFactory.setPath(StringUtils.urlDecode(node.getFirstChild().getTextContent()));
        return davFileFactory;
    }

    private static DavFileFactory metaDate(Node node) {
        NodeList list = node.getChildNodes();
        DavFileFactory davFileFactory = new DavFileFactory();
        for (int i = 0; i < list.getLength(); i++) {
            node = list.item(i);
            String name = node.getNodeName();
            if ("d:getlastmodified".equals(name.toLowerCase())) {
                davFileFactory.setLastModified(node.getTextContent());
            } else if ("d:getcontentlength".equals(name.toLowerCase())) {
                davFileFactory.setSize(Long.valueOf(node.getTextContent()));
            } else if ("d:displayname".equals(name.toLowerCase())) {
                davFileFactory.setName(node.getTextContent());
            }
        }
        return davFileFactory;
    }
}