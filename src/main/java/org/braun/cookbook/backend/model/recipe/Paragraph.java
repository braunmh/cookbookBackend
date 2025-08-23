package org.braun.cookbook.backend.model.recipe;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author mbraun
 */
public class Paragraph extends ContentElement<Paragraph> implements StructureElement {

    @Override
    public Paragraph value(String value) {
        setValue(value);
        return this;
    }
    
    @Override
    public String getTagName() {
        return "p";
    }
    
   @Override
   public void toSaxStream(ContentHandler contentHandler) throws SAXException {
      if (isEmpty()) {
         return;
      }
      contentHandler.startElement("", "p", "p", EMPTY_ATTRIBUTES);
      characters(contentHandler, getValue());
      contentHandler.endElement("", "p", "p");
   }
}
