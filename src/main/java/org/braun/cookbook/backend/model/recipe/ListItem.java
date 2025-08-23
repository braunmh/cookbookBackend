package org.braun.cookbook.backend.model.recipe;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author mbraun
 */
public class ListItem extends ContentElement<ListItem> {

    @Override
    public ListItem value(String value) {
        setValue(value);
        return this;
    }

    @Override
    public String getTagName() {
        return "li";
    }

    @Override
    public void toSaxStream(ContentHandler contentHandler) throws SAXException {
        if (isEmpty()) {
            return;
        }
        contentHandler.startElement("", "li", "li", EMPTY_ATTRIBUTES);
        characters(contentHandler, getValue());
        contentHandler.endElement("", "li", "li");
    }
}
