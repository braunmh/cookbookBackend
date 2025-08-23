package org.braun.cookbook.backend.model.recipe;

import java.util.StringTokenizer;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author mbraun
 */
public class Ingredient extends ContentElement<Ingredient> {

    private String count;

    private String unit;

    private enum Type {
        numeric, to, literal;
    }

   @Override
   public void toSaxStream(ContentHandler contentHandler) throws SAXException {
      if (isEmpty()) {
         return;
      }
      AttributesImpl attrs = new AttributesImpl();
      if (count != null && count.length() > 0) {
         addAttribute(attrs, "count", count);
      }

      if (unit != null && unit.length() > 0) {
         addAttribute(attrs, "unit", unit);
      }
      contentHandler.startElement("", "Ingredient", "Ingredient", attrs);
      characters(contentHandler, getValue());
      contentHandler.endElement("", "Ingredient", "Ingredient");

   }
    public static Ingredient parse(String value) {
        if (value == null || value.trim().length() == 0) {
            return null;
        }

        StringTokenizer st = new StringTokenizer(value, " -–", true);
        Ingredient ingredient = new Ingredient();
        if (st.countTokens() == 1) {
            ingredient.setValue(value.trim());
            return ingredient;
        }

        String[] values = new String[st.countTokens()];
        int length = 0;
        boolean circa = false;
        boolean first = true;
        while (st.hasMoreTokens()) {
            String v = st.nextToken();
            if (v.trim().length() == 0) {
                continue;
            }
            if (first && !circa && (v.equals("circa") || v.equals("ca."))) {
                circa = true;
                continue;
            }
            first = false;
            values[length++] = v;
        }
        Type[] types = new Type[length];
        int firstLiteral = 0;
        for (int i = 0; i < length; i++) {
            switch (values[i].charAt(0)) {
                case '–':
                case '-':
                    types[i] = Type.to;
                    break;
                case '¼':
                    values[i] = "0.25";
                    types[i] = Type.numeric;
                    break;
                case '½':
                    values[i] = "0.5";
                    types[i] = Type.numeric;
                    break;
                case '¾':
                    values[i] = "0.75";
                    types[i] = Type.numeric;
                    break;
                case '\u215B':
                    values[i] = "0.125";
                    types[i] = Type.numeric;
                    break;
                default:
                    char c = values[i].charAt(0);
                    if (c >= '0' && c <= '9') {
                        types[i] = Type.numeric;
                        values[i] = values[i].replace(',', '.');
                        values[i] = values[i].replace('o', '0');
                        values[i] = values[i].replace('O', '0');
                    } else {
                        types[i] = Type.literal;
                        firstLiteral = i;
                    }
            }
            if (types[i] == Type.literal) {
                break;
            }
        }

        if (types[0] != Type.numeric) {
            StringBuilder content = new StringBuilder();
            for (int i = 0; i < length; i++) {
                if (i > firstLiteral) {
                    content.append(" ");
                }
                content.append(values[i]);
            }
            if (circa) {
                content.append(", circa");
            }
            ingredient.setValue(content.toString());
            return ingredient;
        }
        int toPosition;

        for (toPosition = 0; toPosition < firstLiteral; toPosition++) {
            if (types[toPosition] == Type.to) {
                break;
            }
        }
        StringBuilder count1 = new StringBuilder(values[0]);
        if (types[1] == Type.numeric) {
            count1.append(" ").append(values[1]);
        }

        StringBuilder count2 = null;
        if (toPosition > 0 && toPosition < length - 2) {
            if (types[toPosition + 1] == Type.numeric) {
                count2 = new StringBuilder(values[toPosition + 1]);
            }
            if (types[toPosition + 2] == Type.numeric) {
                if (null == count2) {
                    count2 = new StringBuilder(values[toPosition + 2]);
                } else {
                    count2.append(" ").append(values[toPosition + 2]);
                }
            }
        }

        String unit;
        if (Units.getInstance().containsUnit(values[firstLiteral])) {
            unit = Units.getInstance().getUnit(values[firstLiteral]);
            firstLiteral++;
        } else {
            unit = "";
        }
        StringBuilder content = new StringBuilder();
        for (int i = firstLiteral; i < length; i++) {
            if (i > firstLiteral) {
                content.append(" ");
            }
            content.append(values[i]);
        }
        if (circa) {
            content.append(", circa");
        }
        ingredient.setValue(content.toString());
        if (count2 != null) {
            ingredient.setCount(convertCount(count1.toString()) + "-" + convertCount(count2.toString()));
        } else {
            ingredient.setCount(convertCount(count1.toString()));
        }
        // für Situationen in denen gramm mit gr bzw gr. abgekürzt wird
        if ("lg".equals(unit) && ingredient.getCount().length() > 1) {
            unit = "g";
        }
        if ("".equals(unit)) {
            String[] v = countContainsUnit(ingredient.getCount());
            if (null != v) {
                ingredient.setCount(v[0]);
                unit = v[1];
            }
        }
        ingredient.setUnit(unit);
        return ingredient;
    }

    private static String[] countContainsUnit(String value) {
        if (null == value || value.length() == 0) {
            return null;
        }
        char[] chars = value.toCharArray();
        for (int i = value.length() - 2; i >= 0; i--) {
            if (Character.isDigit(chars[i])) {
                String[] values = new String[2];
                values[0] = value.substring(0, i + 1);
                values[1] = value.substring(i + 1);
                if (Units.getInstance().containsUnit(values[1])) {
                    values[1] = Units.getInstance().getUnit(values[1]);
                    return values;
                }
                return null;
            }
        }
        return null;
    }

    private static String convertCount(String value) {
        try {
            return DecimalFraction.toString(DecimalFraction.parseDecimalFraction(value));
        } catch (NumberFormatException e) {
            return value;
        }
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public Ingredient count(String value) {
        count = value;
        return this;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Ingredient unit(String value) {
        unit = value;
        return this;
    }

    @Override
    public Ingredient value(String value) {
        setValue(value);
        return this;
    }

    @Override
    public String getTagName() {
        return "Ingredient";
    }

}
