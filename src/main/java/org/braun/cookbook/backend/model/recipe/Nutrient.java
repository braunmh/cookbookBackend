package org.braun.cookbook.backend.model.recipe;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author mbraun
 */
public class Nutrient implements EmptyElement {

    private String count;

    private String unit;

    private String content;

    public String getCount() {
        return count;
    }

    @Override
    public void toSaxStream(ContentHandler contentHandler) throws SAXException {
        if (isEmpty()) {
            return;
        }
        AttributesImpl attrs = new AttributesImpl();
        if (unit != null) {
            addAttribute(attrs, "unit", unit);
        }
        if (count != null) {
            addAttribute(attrs, "count", count);
        }
        if (content != null) {
            addAttribute(attrs, "content", content);
        }
        contentHandler.startElement("", "Nutrient", "Nutrient", attrs);
        contentHandler.endElement("", "Nutrient", "Nutrient");
    }

    public void setCount(String count) {
        this.count = count;
    }

    public Nutrient count(String value) {
        count = value;
        return this;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Nutrient unit(String value) {
        unit = value;
        return this;
    }

    @Override
    public String getTagName() {
        return "Ingredient";
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Nutrient content(String value) {
        content = value;
        return this;
    }

    @Override
    public boolean isEmpty() {
        return unit == null || unit.isBlank() || count == null || count.isBlank();
    }

}
