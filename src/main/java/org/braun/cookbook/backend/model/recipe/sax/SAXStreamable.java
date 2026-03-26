package org.braun.cookbook.backend.model.recipe.sax;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author mbraun
 */
public interface SAXStreamable {

   public static final String PCDATA = "PCDATA";
   public static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();
   
   void toSaxStream(ContentHandler contentHandler) throws SAXException;
   
   boolean isEmpty();
   
   default void addAttribute(AttributesImpl attrs, String name, String value) throws SAXException {
        if (value == null || value.length() == 0) {
            return;
        }
        attrs.addAttribute("", name, name, PCDATA, value);
    }
   
   
   default void addAttribute(AttributesImpl attrs, String name, Integer value) throws SAXException {
        if (value == null) {
            return;
        }
        attrs.addAttribute("", name, name, PCDATA, String.valueOf(value));
    }
   
   default void addAttribute(AttributesImpl attrs, String name, Long value) throws SAXException {
        if (value == null) {
            return;
        }
        attrs.addAttribute("", name, name, PCDATA, String.valueOf(value));
    }
   
   default void characters(ContentHandler contentHandler, String value) throws SAXException {
       if (value == null) {
           return;
       }
       contentHandler.characters(value.toCharArray(), 0, value.length());
   }
           
}
