package org.braun.cookbook.backend.model.recipe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author mbraun
 */
public class Nutrients implements EmptyElement {

    private final List<Nutrient> nutrients;

    private String unit;

    public Nutrients() {
        nutrients = new ArrayList<>();
    }

    @Override
    public boolean isEmpty() {
        return !nutrients.stream().anyMatch(i -> !i.isEmpty());
    }

    @Override
    public String getTagName() {
        return "Nutrients";
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
        contentHandler.startElement("", "Nutrients", "Nutrients", attrs);
        for (Nutrient nu : nutrients) {
            nu.toSaxStream(contentHandler);
        }
        contentHandler.endElement("", "Nutrients", "Nutrients");
    }

    private char[] c;
    private int cur = 0;
    private boolean eol = false;

    public Nutrients(String line) {
        this();
        if (line == null) {
            return;
        }
        if (!line.startsWith("Nährwert") && !line.toLowerCase().startsWith("pro")) {
            return;
        }
        c = line.toCharArray();

        String value = getNextWord();
        if (value == null) {
            return;
        }
        if (!"pro".equalsIgnoreCase(value)) {
            value = getNextWord();
        }
        if (value.equals(",") || value.equalsIgnoreCase("pro")) {
            unit = getNextWord();
            getNextWord();
        }
        String[] values = new String[3];
        int i = 0;
        while (!eol) {
            value = getNextWord();
            if (value.equals(",")) {
                switch (i) {
                    case 2:
                        add(new Nutrient().count(values[0]).unit(values[1]));
                        break;
                    case 3:
                        add(new Nutrient().count(values[0]).unit(values[1]).content(values[2]));
                        break;
                }
                i = 0;
                continue;
            }
            if (i < 3) {
                values[i] = value;
                i++;
            } else {
                values[2] = values[2] + " " + value;
            }
        }
    }

    private String getNextWord() {
        if (cur >= c.length - 1) {
            eol = true;
            return ",";
        }
        // skip leading blanks
        while (c[cur] == ' ') {
            cur++;
        }
        if (c[cur] == ':') {
            cur++;
            return ":";
        }
        if (",/".indexOf(c[cur]) > -1) {
            cur++;
            return ",";
        }
        int i = cur;
        cur++;
        boolean eow = false;
        while (cur < c.length) {
            switch (c[cur]) {
                case ' ':
                    eow = true;
                    break;
                case '/':
                    eow = true;
                    break;
                case ':':
                    eow = true;
                    break;
                case ',':
                    if (cur + 1 < c.length && c[cur + 1] == ' ') {
                        eow = true;
                    }
                    break;
            }
            if (eow) {
                break;
            }
            cur++;
        }
        if (i < cur) {
            return new String(c, i, cur - i);
        }
        return ",";
    }

    public static boolean isNutrients(String line) {
        if (line == null) {
            return false;
        }
        return (line.toLowerCase().startsWith("nährwert")
                || line.toLowerCase().startsWith("pro portion"));
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Nutrients unit(String value) {
        unit = value;
        return this;
    }

    public List<Nutrient> getNutrients() {
        return nutrients;
    }

    public final Nutrients add(Nutrient value) {
        getNutrients().add(value);
        return this;
    }

    public String toText() {
        if (nutrients == null || nutrients.isEmpty()) {
            return null;
        }

        StringBuilder value = new StringBuilder("Nährwerte");
        if (getUnit() != null) {
            value.append(" / ").append(unit);
        }
        value.append(": ");
        Iterator<Nutrient> iter = nutrients.iterator();
        int i = 0;
        while (iter.hasNext()) {
            if (i > 0) {
                value.append(", ");
            }
            value.append(iter.next().toText());
            i++;
        }

        return value.toString();
    }

}
