package org.braun.cookbook.backend.mapping;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.braun.cookbook.backend.model.recipe.Ingredient;
import org.braun.cookbook.backend.model.recipe.Ingredients;
import org.braun.cookbook.backend.model.recipe.Units;
import org.braun.cookbook.util.xml.XMLWriter;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

public class IngredientsMapper {

    private static final IngredientsMapper INSTANCE = new IngredientsMapper();
    
    private IngredientsMapper() {}
    
    public static IngredientsMapper getInstance() {
        return INSTANCE;
    }
    
    public List<Ingredients> map(String value) {
        if (value == null || value.trim().length() == 0) {
            return null;
        }
        value = value.replaceAll("\u00a0", " ");
        List<Ingredients> ins = new ArrayList<>();
        try {
            XMLReader reader = new Parser();
            reader.setFeature(Parser.namespacePrefixesFeature, false);
            RawFilter rawFilter = new RawFilter();
            rawFilter.setParent(reader);
            IngredientsFilter ingredientsFilter = new IngredientsFilter(ins);
            ingredientsFilter.setParent(rawFilter);
            ingredientsFilter.parse(new InputSource(new StringReader(value)));
        } catch (SAXException | IOException e) {
        }
        return ins;
    }

    public String map(List<Ingredients> ingredientsList) {
        if (ingredientsList == null || ingredientsList.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Ingredients ins : ingredientsList) {
            if (ins == null) {
                continue;
            }
            if (null != ins.getTitle() && ins.getTitle().length() > 0) {
                sb.append("<p>").append(XMLWriter.escapeElementEntities(ins.getTitle())).append("</p>");
            }
            if (ins.getIngredients() == null || ins.getIngredients().isEmpty()) {
                continue;
            }
            sb.append("<ul>");
            for (Ingredient ingredient : ins.getIngredients()) {
                sb.append("<li>");
                if (StringUtils.isNotEmpty(ingredient.getCount())) {
                    sb.append(ingredient.getCount()).append(" ");
                }
                if (StringUtils.isNotEmpty(ingredient.getUnit())) {
                    sb.append(Units.getInstance().getDescription(ingredient.getUnit())).append(" ");
                }
                sb.append(XMLWriter.escapeElementEntities(ingredient.getValue())).append("</li>");
            }
            sb.append("</ul>");
        }
        return sb.toString();
    }

    static class IngredientsFilter extends XMLFilterImpl {

        CharArrayWriter writer;
        List<Ingredients> ingredientsList;
        Ingredients ingredients;

        public IngredientsFilter(List<Ingredients> ingredientsList) {
            this.ingredientsList = ingredientsList;
        }

        @Override
        public void startDocument() throws SAXException {
            writer = new CharArrayWriter();
            ingredients = null;
        }

        @Override
        public void endDocument() throws SAXException {
            writer = null;
            if (ingredients != null) {
                ingredientsList.add(ingredients);
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if ("ul".equals(qName)) {
                if (ingredients == null) {
                    ingredients = new Ingredients();
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (null != qName) {
                switch (qName) {
                    case "p" -> {
                        if (null != ingredients) {
                            ingredientsList.add(ingredients);
                        }
                        ingredients = new Ingredients().title(getBuffer());
                    }
                    case "ul" -> {
                        if (null != ingredients) {
                            ingredientsList.add(ingredients);
                            ingredients = null;
                        }
                    }
                    case "li" ->
                        ingredients.add(Ingredient.parse(getBuffer()));
                    default -> {
                    }
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            writer.write(ch, start, length);
        }

        private String getBuffer() {
            boolean lastBlank = true;
            char[] temp = new char[writer.size()];
            int length = 0;
            for (char c : writer.toCharArray()) {
                if (" \n\t\r\u00a0".indexOf(c) > -1) {
                    if (lastBlank) {
                        continue;
                    }
                    temp[length++] = ' ';
                    lastBlank = true;
                } else {
                    lastBlank = false;
                    temp[length++] = c;
                }
            }
            if (length > 1 && temp[length - 1] == ' ') {
                length--;
            }
            if (length == writer.size()) {
                writer.reset();
                return new String(temp);
            }
            writer.reset();
            char[] value = new char[length];
            System.arraycopy(temp, 0, value, 0, length);
            return new String(value);
        }
    }

    static class RawFilter extends XMLFilterImpl {

        static final AttributesImpl attrs = new AttributesImpl();
        CharArrayWriter writer;
        boolean isElementList;
        static final Entry[] elementsBlock = new Entry[]{
            new Entry("p", "p"),
            new Entry("li", "li"),
            new Entry("h1", "p"),
            new Entry("h2", "p"),
            new Entry("h3", "p"),
            new Entry("h4", "p"),
            new Entry("h5", "p"),
            new Entry("h6", "p"),
            new Entry("h7", "p"),
            new Entry("tr", "li")};

        static {
            Arrays.sort(elementsBlock);
        }
        static final Entry[] elementsInline = new Entry[]{
            new Entry("td", " ")
        };

        static {
            Arrays.sort(elementsInline);
        }
        static final Entry[] elementsList = new Entry[]{
            new Entry("table", "ul"),
            new Entry("ol", "ul"),
            new Entry("ul", "ul")
        };

        static {
            Arrays.sort(elementsList);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if ("br".equals(qName)) {
                if (isElementList) {
                    writer.write(' ');
                    return;
                }
                char[] value = getBuffer();
                if (value.length > 0) {
                    super.startElement(uri, "p", "p", attrs);
                    super.characters(value, 0, value.length);
                    super.endElement(uri, "p", "p");
                }
                return;
            }
            if (Arrays.binarySearch(elementsInline, new Entry(qName)) > -1) {
                writer.write(' ');
                return;
            }
            int indexElement;
            if ((indexElement = Arrays.binarySearch(elementsList, new Entry(qName))) > -1) {
                isElementList = true;
                if (writer.size() > 0) {
                    char[] value = getBuffer();
                    super.startElement(uri, "p", "p", attrs);
                    super.characters(value, 0, value.length);
                    super.endElement(uri, "p", "p");
                }
                super.startElement(uri, elementsList[indexElement].value, elementsList[indexElement].value, attrs);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            int indexElement;
            if ((indexElement = Arrays.binarySearch(elementsList, new Entry(qName))) > -1) {
                isElementList = false;
                super.endElement(uri, elementsList[indexElement].value, elementsList[indexElement].value);
                return;
            }
            if ((indexElement = Arrays.binarySearch(elementsBlock, new Entry(qName))) > -1) {
                char[] value = getBuffer();
                if (value.length > 0) {
                    super.startElement(uri, elementsBlock[indexElement].value, elementsBlock[indexElement].value, attrs);
                    super.characters(value, 0, value.length);
                    super.endElement(uri, elementsBlock[indexElement].value, elementsBlock[indexElement].value);
                }
            }
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            writer = new CharArrayWriter();
            isElementList = false;
        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
            writer = null;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            writer.write(ch, start, length);
        }

        private char[] getBuffer() {
            String value = writer.toString().trim();
            writer.reset();
            return value.toCharArray();
        }
    }

    static class Entry implements Comparable<Entry> {

        String key, value;

        public Entry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public Entry(String key) {
            this.key = key;
            this.value = key;
        }

        @Override
        public int compareTo(Entry o) {
            if (o == null) {
                return -1;
            }
            return key.compareTo(o.key);
        }
    }
}
