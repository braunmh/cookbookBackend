package org.braun.cookbook.backend.mapping;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.braun.cookbook.backend.model.recipe.Description;
import org.braun.cookbook.backend.model.recipe.Heading;
import org.braun.cookbook.backend.model.recipe.ListElement;
import org.braun.cookbook.backend.model.recipe.ListItem;
import org.braun.cookbook.backend.model.recipe.OrderedList;
import org.braun.cookbook.backend.model.recipe.Paragraph;
import org.braun.cookbook.backend.model.recipe.StructureElement;
import org.braun.cookbook.backend.model.recipe.UnorderedList;
import org.braun.cookbook.util.xml.AbstractXMLReader;
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
public class DescriptionMapper {

    private static final DescriptionMapper INSTANCE = new DescriptionMapper();
    
    private static final Logger LOG = LogManager.getLogger();
    
    private static final List<String> HEADINGS = List.of("h1", "h2", "h3", "h4", "h5", "h6", "h7", "h8", "h9");
    private static final List<String> PARAGRAPHS = List.of("p", "br", "ul", "ol", "li");

    private DescriptionMapper() {}
    
    public static DescriptionMapper getInstance() {
        return INSTANCE;
    }
    
    public Description map(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        value = value.replaceAll("\u00a0", " ");
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
            LOG.error("Parsing: " + value, e);
        }
        return description;
    }

    public String map(Description value) {
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
            LOG.error("Convert Dewcription to HTML", ex);
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
            if (null != qName) {
                switch (qName) {
                    case "p" -> {
                        Paragraph p = new Paragraph();
                        p.setValue(getBuffer());
                        description.add(p);
                    }
                    case "h2" -> {
                        Heading heading = new Heading();
                        heading.setValue(getBuffer());
                        description.add(heading);
                    }
                    case "li" -> {
                        ListItem listItem = new ListItem();
                        listItem.setValue(getBuffer());
                        list.add(listItem);
                    }
                    default -> {
                    }
                }
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

    class DescriptionReader extends AbstractXMLReader {

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
            if (HEADINGS.contains(elementName)) {
                return "h2";
            }
            if (PARAGRAPHS.contains(elementName)) {
                return elementName;
            }
            return null;
        }
    }

}
