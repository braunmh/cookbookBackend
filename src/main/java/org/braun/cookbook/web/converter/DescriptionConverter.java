/*
 * Copyright 2010 Michael H. Braun
 * 
 */
package org.braun.cookbook.web.converter;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.lang3.StringUtils;
import org.braun.cookbook.backend.model.recipe.Description;
import org.braun.cookbook.backend.model.recipe.Heading;
import org.braun.cookbook.backend.model.recipe.ListElement;
import org.braun.cookbook.backend.model.recipe.ListItem;
import org.braun.cookbook.backend.model.recipe.OrderedList;
import org.braun.cookbook.backend.model.recipe.Paragraph;
import org.braun.cookbook.backend.model.recipe.StructureElement;
import org.braun.cookbook.backend.model.recipe.UnorderedList;
import org.braun.cookbook.backend.model.recipe.sax.AbstractXmlReader;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 *
 * @author mbraun
 */
@FacesConverter(forClass = org.braun.cookbook.backend.model.recipe.Description.class, value = DescriptionConverter.CONVERTER_ID)
public class DescriptionConverter implements Converter<Description> {

    static final transient Logger logger = Logger.getLogger(DescriptionConverter.class.getName());
    static final String CONVERTER_ID = "descriptionConverter";

    @Override
    public Description getAsObject(FacesContext context, UIComponent component, String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        Description description = new Description();
        try {
            XMLReader reader = new Parser();
            reader.setFeature(Parser.namespacePrefixesFeature, false);
            RawFilter rawFilter = new RawFilter();
            rawFilter.setParent(reader);
            DescriptionFilter descriptionFilter = new DescriptionFilter(description);
            descriptionFilter.setParent(rawFilter);
            descriptionFilter.parse(new InputSource(new StringReader(value)));
        } catch (SAXException | IOException e) {
            logger.log(Level.SEVERE, "Parsing: " + value, e);
        }
        return description;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Description value) {
        if (value == null) {
            return null;
        }
        Description description = (Description) value;
        if (description.isEmpty()) {
            return "";
        }
        StringWriter writer = new StringWriter();
        try {
            DescriptionReader reader = new DescriptionReader();
            DescriptionSource inputSource = new DescriptionSource(description);
            TransformerFactory factory = TransformerFactory.newInstance();
            SAXSource saxSource = new SAXSource(reader, inputSource);
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            StreamResult result = new StreamResult(writer);
            transformer.transform(saxSource, result);

            return writer.toString();
        } catch (TransformerException ex) {
            logger.log(Level.SEVERE, "Convert Dewcription to HTML", ex);
        }
        return "";
    }

    class DescriptionFilter extends XMLFilterImpl {

        CharArrayWriter writer;
        ListElement list;
        Description description;

        public DescriptionFilter(Description description) {
            this.description = description;
            writer = new CharArrayWriter();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if ("ul".equals(qName)) {
                list = new UnorderedList();
                description.add(list);
            } else if ("ol".equals(qName)) {
                list = new OrderedList();
                description.add(list);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ("p".equals(qName)) {
                Paragraph p = new Paragraph();
                p.setValue(getBuffer());
                description.add(p);
            } else if ("h2".equals(qName)) {
                Heading heading = new Heading();
                heading.setValue(getBuffer());
                description.add(heading);
            } else if ("li".equals(qName)) {
                ListItem listItem = new ListItem();
                listItem.setValue(getBuffer());
                list.add(listItem);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            writer.write(ch, start, length);
        }

        String getBuffer() {
            String value = writer.toString();
            writer.reset();
            return value;
        }
    }

    class DescriptionReader extends AbstractXmlReader {

        @Override
        public void parse(InputSource source) throws IOException, SAXException {
            Description description = ((DescriptionSource) source).description;
            contentHandler.startDocument();
            for (StructureElement element : description.getContent()) {
                element.toSaxStream(contentHandler);
            }
            contentHandler.endDocument();
        }
    }

    static class DescriptionSource extends InputSource {

        Description description;

        public DescriptionSource(Description description) {
            this.description = description;
        }
    }

    /**
     * Normalize incoming HTML to recognized elements
     */
    class RawFilter extends XMLFilterImpl {

        AttributesImpl attrs;
        boolean chars;
        CharArrayWriter writer;
        int stack;

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            chars = true;
            writer.write(ch, start, length);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            String elementName = recognized(qName);
            if (elementName != null) {
                stack++;
                if (chars && (stack == 0 || "br".equals(qName))) {
                    char[] value = getBuffer();
                    if (value.length > 0) {
                        super.startElement(uri, "p", "p", attrs);
                        super.characters(value, 0, value.length);
                        super.endElement(uri, "p", "p");
                    }
                }
                if ("ul".equals(elementName) || "ol".equals(elementName)) {
                    super.startElement(uri, qName, qName, attrs);
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            String elementName = recognized(qName);
            if (elementName != null) {
                stack--;
                if ("br".equals(qName)) {
                    return;
                }
                if ("ul".equals(elementName) || "ol".equals(elementName)) {
                    super.endElement(uri, elementName, elementName);
                } else {
                    char[] value = getBuffer();
                    if (value.length > 0) {
                        super.startElement(uri, elementName, elementName, attrs);
                        super.characters(value, 0, value.length);
                        super.endElement(uri, elementName, elementName);
                    }
                }
            }
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            attrs = new AttributesImpl();
            chars = false;
            writer = new CharArrayWriter();
            stack = 0;
        }

        @Override
        public void endDocument() throws SAXException {
            char[] value = getBuffer();
            if (value.length > 0) {
                super.startElement("", "p", "p", attrs);
                super.characters(value, 0, value.length);
                super.endElement("", "p", "p");
            }
            super.endDocument();
            writer = null;
        }

        char[] getBuffer() {
            String value = writer.toString().trim();
            chars = false;
            writer.reset();
            return value.toCharArray();
        }

        private String recognized(String elementName) {
            if ("h1 h2 h3 h4 h5 h6 h7 h8 h9".contains(elementName)) {
                return "h2";
            }
            if ("p br ul ol li".contains(elementName)) {
                return elementName;
            }
            return null;
        }
    }
}
