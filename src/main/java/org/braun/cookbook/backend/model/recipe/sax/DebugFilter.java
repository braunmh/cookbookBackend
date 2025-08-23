package org.braun.cookbook.backend.model.recipe.sax;

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
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        write("<" + localName);
        for (int i=0; i <atts.getLength(); i++) {
            write(" " + atts.getQName(i) + "=\"" + atts.getValue(i) + "\"");
        }
        write(">");
        super.startElement(uri, localName, qName, atts);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        write("</" + localName + ">");
        super.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        write(ch, start, length);
        super.characters(ch, start, length); 
    }
    
    private void write(char[] ch, int start, int length) throws SAXException  {
        try {
            writer.write(ch, start, length);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }
    
    private void write(String value) throws SAXException {
        try {
            writer.write(value);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }
}
