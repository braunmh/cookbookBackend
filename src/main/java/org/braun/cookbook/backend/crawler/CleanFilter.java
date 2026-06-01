package org.braun.cookbook.backend.crawler;

import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 *
 * @author mbraun
 */
public class CleanFilter extends XMLFilterImpl {
    
    private final List<String> tags;
    
    private boolean ignore;
    private int stack = 0;
    
    public CleanFilter(List<String> tags) {
        this.tags = tags;
        ignore = false;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        stack++;
        if (ignore) {
            return;
        }
        if (tags.contains(localName)) {
            ignoreTag();
            return;
        }
        super.startElement(uri, localName, qName, atts);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (ignore) {
            if (stack == 0) {
                ignore = false;
            }
            stack--;
            return;
        }
        super.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (!ignore) {
            super.characters(ch, start, length);
        }
    }
 
    private void ignoreTag() {
        stack = 0;
        ignore = true;
    }
}
