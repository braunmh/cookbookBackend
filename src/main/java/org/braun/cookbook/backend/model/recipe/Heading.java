package org.braun.cookbook.backend.model.recipe;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author mbraun
 */
public class Heading extends ContentElement<Heading> implements StructureElement {

    @Override
    public Heading value(String value) {
        setValue(value);
        return this;
    }

    @Override
    public String getTagName() {
        return "h2";
    }

    @Override
    public void toSaxStream(ContentHandler contentHandler) throws SAXException {
        if (isEmpty()) {
            return;
        }
        contentHandler.startElement("", "h2", "h2", EMPTY_ATTRIBUTES);
        contentHandler.characters(getValue().toCharArray(), 0, getValue().length());
        contentHandler.endElement("", "h2", "h2");
    }

}
