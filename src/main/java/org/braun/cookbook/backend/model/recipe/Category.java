package org.braun.cookbook.backend.model.recipe;

import java.util.Objects;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author mbraun
 */
public class Category implements EmptyElement {

    private String name;
    
    @Override
    public boolean isEmpty() {
        return name == null || name.isBlank();
    }

    @Override
    public String getTagName() {
        return "Category";
    }

   @Override
   public void toSaxStream(ContentHandler contentHandler) throws SAXException {
      if (null == name) return;
      AttributesImpl attrs = new AttributesImpl();
      addAttribute(attrs, "name", name);
      contentHandler.startElement("", "Category", "Category", attrs);
      contentHandler.endElement("", "Category", "Category");
   }

   public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Category name(String value) {
        name = value;
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Category other = (Category) obj;
        return Objects.equals(this.name, other.name);
    }
}
