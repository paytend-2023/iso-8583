
package com.easy.iso8583.parse;


import com.easy.iso8583.IsoMessage;
import com.easy.iso8583.IsoType;
import com.easy.iso8583.IsoValue;
import com.easy.iso8583.MessageFactory;
import com.gd.magic.MagicFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

/**
 * This class is used to parse a XML configuration file and configure
 * a MessageFactory with the values from it.
 *
 * @author Enrique Zamudio
 */


public class ConfigParser {
    static Logger log = Logger.getLogger(ConfigParser.class);

    /**
     * Creates a message factory configured from the default file, which is j8583.xml
     * located in the root of the classpath, using the specified ClassLoader.
     */
    public static MessageFactory<IsoMessage> createDefault(
            ClassLoader loader) throws IOException {
        if (loader.getResource("j8583.xml") == null) {
            log.debug("ISO8583 ConfigParser cannot find j8583.xml, returning empty message factory");
            return new MessageFactory<>();
        } else {
            return createFromClasspathConfig(loader, "j8583.xml");
        }
    }

    /**
     * Creates a message factory configured from the default file, which is j8583.xml
     * located in the root of the classpath, using the MessageFactory's
     * ClassLoader.
     */
    public static MessageFactory<IsoMessage> createDefault() throws IOException {
        return createDefault(MessageFactory.class.getClassLoader());
    }

    /**
     * Creates a message factory from the specified path inside the classpath,
     * using the specified ClassLoader.
     */
    public static MessageFactory<IsoMessage> createFromClasspathConfig(
            String path) throws IOException {
        return createFromClasspathConfig(MessageFactory.class.getClassLoader(), path);
    }

    /**
     * Creates a message factory from the specified path inside the classpath,
     * using MessageFactory's ClassLoader.
     */
    public static MessageFactory<IsoMessage> createFromClasspathConfig(
            ClassLoader loader, String path) throws IOException {
        MessageFactory<IsoMessage> mfact = new MessageFactory<>();
        try (InputStream ins = loader.getResourceAsStream(path)) {
            if (ins != null) {
                log.debug("ISO8583 Parsing config from classpath file " + path);
                parse(mfact, new InputSource(ins));
            } else {
                log.error("ISO8583 File not found in classpath:  " + path);
            }
        }
        return mfact;
    }


    protected static <T extends IsoMessage> void parseHeaders(
            final NodeList nodes) throws IOException {
        ArrayList<Element> refs = null;
        for (int i = 0; i < nodes.getLength(); i++) {
            Element elem = (Element) nodes.item(i);
            int type = parseType(elem.getAttribute("type"));
            if (type == -1) {
                throw new IOException("Invalid type for ISO8583 header: " + elem.getAttribute("type"));
            }
            if (elem.getChildNodes() == null || elem.getChildNodes().getLength() == 0) {
                if (elem.getAttribute("ref") != null && !elem.getAttribute("ref").isEmpty()) {
                    if (refs == null) {
                        refs = new ArrayList<>(nodes.getLength() - i);
                    }
                    refs.add(elem);
                } else throw new IOException("Invalid ISO8583 header element");
            }
        }

        if (refs != null) {
            for (Element elem : refs) {
                int type = parseType(elem.getAttribute("type"));
                if (type == -1) {
                    throw new IOException("Invalid type for ISO8583 header: "
                            + elem.getAttribute("type"));
                }
                if (elem.getAttribute("ref") != null && !elem.getAttribute("ref").isEmpty()) {
                    int t2 = parseType(elem.getAttribute("ref"));
                    if (t2 == -1) {
                        throw new IOException("Invalid type reference "
                                + elem.getAttribute("ref") + " for ISO8583 header " + type);
                    }
                }
            }
        }
    }

    protected static <T extends IsoMessage> void parseTemplates(
            final NodeList nodes, final MessageFactory<T> mfact) throws IOException {
        ArrayList<Element> subs = null;
        for (int i = 0; i < nodes.getLength(); i++) {
            Element elem = (Element) nodes.item(i);
            int type = parseType(elem.getAttribute("type"));
            if (type == -1) {
                throw new IOException("Invalid ISO8583 type for template: " + elem.getAttribute("type"));
            }
            if (elem.getAttribute("extends") != null && !elem.getAttribute("extends").isEmpty()) {
                if (subs == null) {
                    subs = new ArrayList<>(nodes.getLength() - i);
                }
                subs.add(elem);
                continue;
            }
            @SuppressWarnings("unchecked")
            T m = (T) new IsoMessage();
            m.setType(type);
            m.setCharacterEncoding(mfact.getCharacterEncoding());
            NodeList fields = elem.getElementsByTagName("field");
            for (int j = 0; j < fields.getLength(); j++) {
                Element f = (Element) fields.item(j);
                if (f.getParentNode() == elem) {
                    final int num = Integer.parseInt(f.getAttribute("num"));
                    IsoValue<?> v = getTemplateField(f);
                    if (v != null) {
                        v.setCharacterEncoding(mfact.getCharacterEncoding());
                    }
                    m.setField(num, v);
                }
            }
        }
        if (subs != null) {
            for (Element elem : subs) {
                int type = parseType(elem.getAttribute("type"));
                int ref = parseType(elem.getAttribute("extends"));
                if (ref == -1) {
                    throw new IllegalArgumentException("Message template "
                            + elem.getAttribute("type") + " extends invalid template "
                            + elem.getAttribute("extends"));
                }

                @SuppressWarnings("unchecked")
                T m = (T) new IsoMessage();
                m.setType(type);
                m.setCharacterEncoding(mfact.getCharacterEncoding());

                NodeList fields = elem.getElementsByTagName("field");
                for (int j = 0; j < fields.getLength(); j++) {
                    Element f = (Element) fields.item(j);
                    int num = Integer.parseInt(f.getAttribute("num"));
                    if (f.getParentNode() == elem) {
                        IsoValue<?> v = getTemplateField(f);
                        if (v != null) {
                            v.setCharacterEncoding(mfact.getCharacterEncoding());
                        }
                        m.setField(num, v);
                    }
                }
            }
        }
    }

    /**
     * Creates an IsoValue from the XML definition in a message template.
     * If it's for a toplevel field and the message factory has a codec for this field,
     * that codec is assigned to that field. For nested fields, a CompositeField is
     * created and populated.
     */
    protected static IsoValue<?> getTemplateField(Element f) {
        final String typedef = f.getAttribute("type");
        if ("exclude".equals(typedef)) {
            return null;
        }
        int length = 0;
        if (f.getAttribute("length").length() > 0) {
            length = Integer.parseInt(f.getAttribute("length"));
        }
        final IsoType itype = IsoType.valueOf(typedef);
        final String v;
        if (f.getChildNodes().getLength() == 0) {
            v = "";
        } else {
            v = f.getChildNodes().item(0).getNodeValue();
        }

        IsoValue<?> rv = itype.needsLength() ? new IsoValue<>(itype, v, length) : new IsoValue<>(itype, v);
        if (f.hasAttribute("tz")) {
            TimeZone tz = TimeZone.getTimeZone(f.getAttribute("tz"));
            rv.setTimeZone(tz);
        }
        return rv;
    }

    protected static <T extends IsoMessage> FieldParseInfo getParser(
            Element f, MessageFactory<T> mfact) {
        IsoType itype = IsoType.valueOf(f.getAttribute("type"));
        int length = 0;
        if (f.getAttribute("length").length() > 0) {
            length = Integer.parseInt(f.getAttribute("length"));
        }
        FieldParseInfo fpi = FieldParseInfo.getInstance(itype, length, mfact.getCharacterEncoding());
        if (f.hasAttribute("tz") && fpi instanceof DateTimeParseInfo) {
            TimeZone tz = TimeZone.getTimeZone(f.getAttribute("tz"));
            ((DateTimeParseInfo) fpi).setTimeZone(tz);
        }
        return fpi;
    }

    protected static <T extends IsoMessage> void parseGuides(
            final NodeList nodes, final MessageFactory<T> mfact) throws IOException {
        ArrayList<Element> subs = null;
        HashMap<Integer, HashMap<Integer, FieldParseInfo>> guides = new HashMap<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element elem = (Element) nodes.item(i);
            int type = parseType(elem.getAttribute("type"));
            if (type == -1) {
                throw new IOException("Invalid ISO8583 type for parse guide: " + elem.getAttribute("type"));
            }
            if (elem.getAttribute("extends") != null && !elem.getAttribute("extends").isEmpty()) {
                if (subs == null) {
                    subs = new ArrayList<>(nodes.getLength() - i);
                }
                subs.add(elem);
                continue;
            }
            HashMap<Integer, FieldParseInfo> parseMap = new HashMap<>();
            NodeList fields = elem.getElementsByTagName("field");
            for (int j = 0; j < fields.getLength(); j++) {
                Element f = (Element) fields.item(j);
                if (f.getParentNode() == elem) {
                    int num = Integer.parseInt(f.getAttribute("num"));
                    parseMap.put(num, getParser(f, mfact));
                }
            }
            mfact.setParseMap(type, parseMap);
            guides.put(type, parseMap);
        }
        if (subs != null) {
            for (Element elem : subs) {
                int type = parseType(elem.getAttribute("type"));
                int ref = parseType(elem.getAttribute("extends"));
                if (ref == -1) {
                    throw new IllegalArgumentException("Message template "
                            + elem.getAttribute("type") + " extends invalid template "
                            + elem.getAttribute("extends"));
                }
                HashMap<Integer, FieldParseInfo> parent = guides.get(ref);
                if (parent == null) {
                    throw new IllegalArgumentException("Parsing guide "
                            + elem.getAttribute("type") + " extends nonexistent guide "
                            + elem.getAttribute("extends"));
                }
                HashMap<Integer, FieldParseInfo> child = new HashMap<>(parent);
                List<Element> fields = getDirectChildrenByTagName(elem);
                for (Element f : fields) {
                    int num = Integer.parseInt(f.getAttribute("num"));
                    String typedef = f.getAttribute("type");
                    if ("exclude".equals(typedef)) {
                        child.remove(num);
                    } else {
                        child.put(num, getParser(f, mfact));
                    }
                }
                mfact.setParseMap(type, child);
                guides.put(type, child);
            }
        }
    }

    private static List<Element> getDirectChildrenByTagName(Element elem) {
        List<Element> childElementsByTagName = new ArrayList<>();
        NodeList childNodes = elem.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element childElem = (Element) childNodes.item(i);
                if (childElem.getTagName().equals("field")) {
                    childElementsByTagName.add(childElem);
                }
            }
        }
        return childElementsByTagName;
    }

    /**
     * Reads the XML from the stream and configures the message factory with its values.
     *
     * @param mfact  The message factory to be configured with the values read from the XML.
     * @param source The InputSource containing the XML configuration.
     */
    protected static <T extends IsoMessage> void parse(
            MessageFactory<T> mfact, InputSource source) throws IOException {
        final DocumentBuilderFactory docfact = DocumentBuilderFactory.newInstance();
        DocumentBuilder docb;
        Document doc;
        try {
            docb = docfact.newDocumentBuilder();
            docb.setEntityResolver(new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId) {
                    if (systemId.contains("j8583.dtd")) {
                        URL dtd = getClass().getResource("j8583.dtd");
                        if (dtd == null) {
                            log.warn("Cannot find j8583.dtd in classpath. j8583 config files will not be validated.");
                        } else {
                            return new InputSource(dtd.toString());
                        }
                    }
                    return null;
                }
            });
            doc = docb.parse(source);
        } catch (SAXException | ParserConfigurationException ex) {
//            log.error("ISO8583 Cannot parse XML configuration", ex);
            return;
        }
        final Element root = doc.getDocumentElement();

        parseHeaders(root.getElementsByTagName("header"));
        parseTemplates(root.getElementsByTagName("template"), mfact);
        //Read the parsing guides
        parseGuides(root.getElementsByTagName("parse"), mfact);
    }

    /**
     * Configures a MessageFactory using the default configuration file j8583.xml. This is useful
     * if you have a MessageFactory created using Spring for example.
     */
    public static <T extends IsoMessage> void configureFromDefault(
            MessageFactory<T> mfact) throws IOException {
        if (mfact.getClass().getClassLoader().getResource("j8583.xml") == null) {
            log.warn("ISO8583 config file j8583.xml not found!");
        } else {
            configureFromClasspathConfig(mfact, "j8583.xml");
        }
    }

    /**
     * This method attempts to open a stream from the XML configuration in the specified URL and
     * configure the message factory from that config.
     */
    public static <T extends IsoMessage> void configureFromUrl(
            MessageFactory<T> mfact, URL url) throws IOException {
        try (InputStream stream = url.openStream()) {
            parse(mfact, new InputSource(stream));
        }
    }

    /**
     * Configures a MessageFactory using the configuration file at the path specified (will be searched
     * within the classpath using the MessageFactory's ClassLoader). This is useful for configuring
     * Spring-bound instances of MessageFactory for example.
     */
    public static <T extends IsoMessage> void configureFromClasspathConfig(
            MessageFactory<T> mfact, String path) throws IOException {
        try (InputStream ins = mfact.getClass().getClassLoader().getResourceAsStream(path)) {
            if (ins != null) {
                log.debug("ISO8583 Parsing config from classpath file " + path);
                parse(mfact, new InputSource(ins));
            } else {
                log.warn("ISO8583 File not found in classpath: " + path);
            }
        }
    }


    /**
     * Parses a message type expressed as a hex string and returns the integer number.
     * For example, "0200" or "200" return the number 512 (0x200)
     */
    private static int parseType(String type) {
        if (type.length() % 2 == 1) {
            type = "0" + type;
        }
        if (type.length() != 4) {
            return -1;
        }
        return ((type.charAt(0) - 48) << 12) | ((type.charAt(1) - 48) << 8)
                | ((type.charAt(2) - 48) << 4) | (type.charAt(3) - 48);
    }

}
