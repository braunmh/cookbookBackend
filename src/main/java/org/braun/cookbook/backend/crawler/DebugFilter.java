package org.braun.cookbook.backend.crawler;

import java.io.IOException;
import java.io.Writer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 *
 * @author mbraun
 */
public class DebugFilter extends XMLFilterImpl {
    
    private final Writer writer;
    
    public DebugFilter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        try {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        } catch (IOException e) {
            
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        super.startElement(uri, localName, qName, atts);
        try {
            writer.write("<");
            writer.write(localName);
            for (int i = 0; i < atts.getLength(); i++) {
                String attName = atts.getQName(i);
                if (attName.startsWith("_:")) {
                    attName = "overwritten_" + attName.substring(2);
                }
                attName = attName.replace(':', '_');
                writer.write(' ');
                writer.write(attName);
                writer.write("=\"");
                writer.write(escapeAttribute(atts.getValue(i)));
                writer.write('\"');
            }
            writer.write(">");
        } catch (IOException e) {
            
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        try {
            writer.write("</");
            writer.write(localName);
            writer.write(">");
        } catch (IOException e) {
            
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        try { 
            for (int i = start; i < start + length; i++) {
                switch (ch[i]) {
                    case '>' -> writer.write("&gt;");
                    case '<' -> writer.write("&lt;");
                    case '&' -> writer.write("&amp;");
                    default -> writer.write(ch[i]);
                }
            }
        } catch (IOException e) {
            
        }
    }
    
    private String escapeAttribute(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder res = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '>' -> res.append("&gt;");
                case '<' -> res.append("&lt;");
                case '&' -> res.append("&amp;");
                case '"' -> res.append("&quot;");
                default -> res.append(c);
            }
        }
        return res.toString();
    }
}
