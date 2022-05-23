package edu.harvard.iq.dataverse.util.urlrewrite;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class RewrittenURLs {

    private static final Logger logger = Logger.getLogger(RewrittenURLs.class.getCanonicalName());

    private static final RewrittenURLs INSTANCE = new RewrittenURLs();
    private Map<String, String> URLS;

    private RewrittenURLs() {
    }

    public static RewrittenURLs getInstance() {
        return INSTANCE;
    }

    public Map<String, String> getURLs() {
        if (URLS == null) {
            URLS = this.loadMappedUrls();
        }
        return URLS;
    }

    private Map<String, String> loadMappedUrls() {
        Map<String, String> urls = new HashMap<>();

        try {
            InputStream input = RewrittenURLs.class.getResourceAsStream("/rewrite-config.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(input);

            NodeList nodeList = doc.getElementsByTagName("url-mapping");

            for (int temp = 0; temp < nodeList.getLength(); temp++) {
                Node node = nodeList.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String id = element.getAttribute("id");
                    String pattern = null;
                    String viewId = null;
                    NodeList patternNodeList = element.getElementsByTagName("pattern");
                    if (patternNodeList.getLength() > 0) {
                        Node patternNode = patternNodeList.item(0);
                        if (patternNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element patternElement = (Element) patternNode;
                            pattern = patternElement.getAttribute("value");
                        }
                    }

                    NodeList viewIdNodeList = element.getElementsByTagName("view-id");
                    if (viewIdNodeList.getLength() > 0) {
                        Node viewIdNode = viewIdNodeList.item(0);
                        if (viewIdNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element viewIdElement = (Element) viewIdNode;
                            viewId = viewIdElement.getAttribute("value");
                        }
                    }

                    if (pattern != null && viewId != null) {
                        urls.put(pattern, viewId);
                        if (id != null) {
                            logger.log(Level.INFO, "Registering url mapping id {0}: pattern {1} -> view-id {2} ", new Object[]{id, pattern, viewId});
                        } else {
                            logger.log(Level.INFO, "Registering url mapping: pattern {0}-> view-id {1}", new Object[]{pattern, viewId});
                        }
                    }
                }
            }

        } catch (ParserConfigurationException | IOException | SAXException ex) {
            logger.warning("Exception in loadMappedUrls: " + ex);
        }
        return urls;
    }
}
