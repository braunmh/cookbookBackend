package org.braun.cookbook.backend.model.recipe;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author mbraun
 */
public class Source extends ContentElement<Source> {

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Source url(String value) {
        url = value;
        return this;
    }

    @Override
    public Source value(String value) {
        setValue(value);
        return this;
    }

    @Override
    public String getTagName() {
        return "Source";
    }

    @Override
    public void toSaxStream(ContentHandler contentHandler) throws SAXException {
        if (isEmpty()) {
            return;
        }
        AttributesImpl attrs = new AttributesImpl();
        if (url != null) {
            addAttribute(attrs, "url", url);
        }
        contentHandler.startElement("", "Source", "Source", attrs);
        characters(contentHandler, getValue());
        contentHandler.endElement("", "Source", "Source");
    }
}
