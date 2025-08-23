package org.braun.cookbook.backend.model.recipe;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author mbraun
 */
public class Yield extends ContentElement<Yield> {

    private String unit;

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Yield unit(String value) {
        unit = value;
        return this;
    }

    @Override
    public Yield value(String value) {
        setValue(value);
        return this;
    }

    @Override
    public void toSaxStream(ContentHandler contentHandler) throws SAXException {
        if (isEmpty()) {
            return;
        }
        AttributesImpl attrs = new AttributesImpl();
        addAttribute(attrs, "unit", getUnit());
        contentHandler.startElement("", "Yield", "Yield", attrs);
        characters(contentHandler, getValue());
        contentHandler.endElement("", "Yield", "Yield");
    }

    @Override
    public String getTagName() {
        return "Yield";
    }

   public static Yield parse(String unit, String content) {
       Yield yield = new Yield();
      if (isNumber(unit)) {
         yield.setUnit(unit);
         if (content.startsWith("Person"))
            yield.setValue("Portionen");
         else {
            yield.setValue("Portionen");
            if (unit != null) {
                switch (unit){
                    case "eine":
                    case "ein":
                        yield.setUnit("1");
                        break;
                    case "zwei":
                        yield.setUnit("2");
                        break;
                    case "drei":
                        yield.setUnit("3");
                        break;
                    case "vier":
                        yield.setUnit("4");
                        break;
                    case "fünf":
                        yield.setUnit("5");
                        break;
                    case "sechs":
                        yield.setUnit("6");
                        break;
                    case "sieben":
                        yield.setUnit("7");
                        break;
                    case "acht":
                        yield.setUnit("8");
                        break;
                    case "neun":
                        yield.setUnit("9");
                        break;
                    case "zehn":
                        yield.setUnit("10");
                        break;
                }
            }
         }
      }
      return yield;
   }
   
   private static boolean isNumber(String value) {
       if (null==value) return false;
      try {
         return Integer.parseInt(value) > 0;
      } catch (NumberFormatException e) {
         return false;
      }
   }
}
