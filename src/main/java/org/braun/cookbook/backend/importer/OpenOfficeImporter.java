/*
 * Created on Jun 6, 2004
 *
 */
package org.braun.cookbook.backend.importer;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.braun.cookbook.backend.model.Keyword;
import org.braun.cookbook.backend.model.Recipe;
import org.braun.cookbook.backend.model.recipe.Category;
import org.braun.cookbook.backend.model.recipe.Ingredient;
import org.braun.cookbook.backend.model.recipe.Ingredients;
import org.braun.cookbook.backend.model.recipe.Nutrients;
import org.braun.cookbook.backend.model.recipe.Paragraph;
import org.braun.cookbook.backend.model.recipe.Yield;
import org.braun.cookbook.backend.process.KeywordFactory;
import org.braun.cookbook.backend.process.RecipeFacade;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * @author mbraun
 *
 * Constructs an XML-SAX-Stream from an given OpenOffice-Document
 */
public class OpenOfficeImporter extends Importer {

    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger();
    NodeList paragraphs;
    int cp;
    int cl;
    Recipe recipe;
    private byte[] image = null;

    public OpenOfficeImporter(RecipeFacade recipeFacade, InputStream inputStream, String pathParent) {
        super(recipeFacade, inputStream, pathParent);
    }

    /*
    * (non-Javadoc) @see org.xml.sax.XMLReader#parse(org.xml.sax.InputSource)
     */
    public void importRecipes() throws IOException, SAXException {
        images = new HashMap<>();
        Document root;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultNSInstance();
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            root = builder.parse(new InputSource(getContent()));
        } catch (IOException | IllegalArgumentException | SAXException | ParserConfigurationException e) {
            LOG.error(e.getMessage(), e);
            return;
        }

        //Document root = (Document) result.getNode();

        NodeList nl = root.getElementsByTagName("office:text");
        if (nl.getLength() == 0) {
            return;
        }
        paragraphs = ((Element) nl.item(0)).getChildNodes();
        
        cl = paragraphs.getLength();
        cp = -1;

        Element paragraph;

        while (null != (paragraph = getNextParagraph())) {
            if (isRecipe(paragraph)) {
                parseRecipe(paragraph);
                if (recipe.getIngredients().isEmpty() || recipe.getDescription().isEmpty()) {
                    continue;
                }
                getRecipeFacade().insert(recipe, getPathParent(), image);
            }
        }
    }

    void parseRecipe(Element reciepElement) throws SAXException {
        recipe = new Recipe();
        image = null;
        recipe.setTitle(getValue(reciepElement));
        Element element;
        while ((element = getNextParagraph()) != null) {
            parseImage(element);
            String v = element.getAttribute("text:style-name");
            if (isRecipe(element)) {
                break;
            }
            if (isCategory(v)) {
                parseCategory(element);
                continue;
            }
            if (isSource(v)) {
                parseSource(element);
                continue;
            }
            if (isYield(v)) {
                parseYield(element);
                continue;
            }
            if (isIngredient(v)) {
                parseIngredient(element);
                continue;
            }
            if (isText(v)) {
                parseText(element);
                continue;
            }
            if (isImage(v)) {
                parseImage(element);
            }
            if (isWebUrl(v)) {
                parseWebUrl(element);
            }
        }

        if (!eof()) {
            resetNextParagraph();
        }
    }

    void parseCategory(Element element) throws SAXException {
        String value = getValue(element);
        if (value != null) {
            StringTokenizer st = new StringTokenizer(getValue(element), ",", false);
            while (st.hasMoreTokens()) {
                Keyword k = KeywordFactory.getInstance().getByName(st.nextToken().trim());
                if (k != null) {
                    recipe.getCategories().add(new Category().name(String.valueOf(k.getId())));
                }
            }
        }
    }

    void parseYield(Element element) throws SAXException {
        Yield yield = new Yield();
        List<String> values = getTabbedValues(element);
        if (values != null && !values.isEmpty()) {
            yield.setUnit(values.get(0));
        }
        if (values != null && values.size() > 1) {
            yield.setValue(values.get(1));
        }
        recipe.setYield(yield);
    }

    void parseIngredient(Element element) throws SAXException {
        List<String> values = getTabbedValues(element);
        recipe.getIngredients().add(new Ingredients());
        if (values != null && values.size() == 1) {
            recipe.getLastIngredients().setTitle(values.get(0));
            element = getNextParagraph();
        }
        while (element != null) {
            if (!isIngredient(element.getAttribute("text:style-name"))) {
                break;
            }
            values = getTabbedValues(element);
            if (values != null && !values.isEmpty()) {
                if (values.size() < 3) {
                    recipe.addIngredients(new Ingredients());
                    recipe.getLastIngredients().setTitle(values.get(0));
                } else {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setCount(values.get(0));
                    ingredient.setUnit(values.get(1));
                    ingredient.setValue(values.get(2));
                    recipe.getLastIngredients().add(ingredient);
                }
            }
            element = getNextParagraph();
        }

        if (!eof()) {
            resetNextParagraph();
        }
    }

    void parseSource(Element element) throws SAXException {
        String value = getValue(element);
        if (value == null) {
            return;
        }
        recipe.getSource().setValue(value);
    }

    void parseText(Element element) throws SAXException {
        String v;
        while (element != null) {
            if (!isText(element.getAttribute("text:style-name"))) {
                break;
            }
            v = getValue(element);
            if (v != null) {
                if (Nutrients.isNutrients(v)) {
                    recipe.setNutrients(new Nutrients(v));
                } else {
                    Paragraph paragraph = new Paragraph();
                    paragraph.setValue(v);
                    recipe.getDescription().add(paragraph);
                }
            }
            element = getNextParagraph();
        }
        if (!eof()) {
            resetNextParagraph();
        }
    }

    String getValue(Element element) throws SAXException {
        NodeList nodes = element.getChildNodes();
        if (nodes == null || nodes.getLength() == 0) {
            return null;
        }
        StringBuilder value = new StringBuilder();
        Node text;
        for (int i = 0; i < nodes.getLength(); i++) {
            text = nodes.item(i);
            if (isDummyNode(text)) {
                continue;
            }
            if (text == null || text.getNodeType() != Node.TEXT_NODE) {
                continue;
            }
            value.append(text.getNodeValue().trim());
        }
        return value.toString();
    }

    List<String> getTabbedValues(Element element) throws SAXException {
        boolean lastWasText = false;
        NodeList nl = element.getChildNodes();
        if (nl == null || nl.getLength() == 0) {
            return null;
        }
        List<String> values = new ArrayList<>();
        Node text;
        for (int i = 0; i < nl.getLength(); i++) {
            text = nl.item(i);
            if (isDummyNode(text)) {
                continue;
            }
            if (text == null || text.getNodeType() != Node.TEXT_NODE) {
                if (!lastWasText) {
                    values.add("");
                }
                lastWasText = false;
                continue;
            }
            values.add(text.getNodeValue().trim());
            lastWasText = true;
        }
        return values;
    }

    boolean isRecipe(Element e) {
        if ("text:h".equals(e.getNodeName())) {
            return true;
        }
        if (("text:p".equals(e.getNodeName()))) {
            String styleName = e.getAttribute("text:style-name");
            if ("P1".equals(styleName) || "Recipe".equals(styleName) || (styleName != null && styleName.startsWith("Heading"))) {
                return true;
            }
        }
        return false;
    }

    boolean isCategory(String s) {
        return s.equals("Category");
    }

    boolean isYield(String s) {
        return s.equals("Yield");
    }

    boolean isIngredient(String s) {
        return s.equals("Ingredient");
    }

    boolean isSource(String s) {
        return s.equals("Source");
    }

    boolean isText(String s) {
        return s.equals("Standard");
    }

    boolean isImage(String s) {
        return "image".equals(s);
    }

    boolean isWebUrl(String s) {
        return "WebUrl".equals(s);
    }

    void parseWebUrl(Element element) throws SAXException {
        String value = getValue(element);
        if (value == null) {
            return;
        }
        recipe.getSource().setUrl(value);
    }

    void parseImage(Element element) throws SAXException {
        NodeList nodes = element.getElementsByTagName("draw:image");
        if (nodes.getLength() > 0) {
            Element imageElement = (Element) nodes.item(0);
            String imageName = imageElement.getAttribute("xlink:href");
            if (null == imageName || imageName.length() == 0) {
                return;
            }

            if (images.containsKey(imageName)) {
                image = images.get(imageName);
            }
        }
    }

    boolean isDummyNode(Node node) {
        return node != null && ("soft-page-break".equals(node.getLocalName())
                || "s".equals(node.getLocalName()));
    }

    Element getNextParagraph() throws SAXException {
        cp++;
        if (cp < cl) {
            return (Element) paragraphs.item(cp);
        }
        return null;
    }

    void resetNextParagraph() {
        cp--;
    }

    boolean eof() {
        return cp >= cl;
    }

    private Map<String, byte[]> images = new HashMap<>();

    private BufferedReader getContent() throws IOException {
        ZipInputStream zis = null;
        FileOutputStream fos = null;
        try {
            zis = new ZipInputStream(getInputStream());
            ZipEntry ze;
            BufferedReader content = null;
            while ((ze = zis.getNextEntry()) != null) {
                if (ze.isDirectory()) {
                    continue;
                }

                if (ze.getName().equals("content.xml")) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] b = new byte[1024];
                    int length;
                    while ((length = zis.read(b)) > 0) {
                        baos.write(b, 0, length);
                    }
                    content = new BufferedReader(
                            new InputStreamReader(
                                    new ByteArrayInputStream(baos.toByteArray()),
                                    "UTF-8"));
                    continue;
                }
                if (ze.getName().startsWith("Pictures")) {
                    String imageName = ze.getName();
                    int lastIndex;
                    String extension;
                    if ((lastIndex = imageName.lastIndexOf('.')) > 0) {
                        extension = imageName.substring(lastIndex);
                    } else {
                        extension = "image/jpeg";
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] b = new byte[1024];
                    int length;
                    while ((length = zis.read(b)) > 0) {
                        baos.write(b, 0, length);
                    }
                    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                    baos.reset();
                    int[] hw = ImageUtil.resizeToWidth(bais, baos, 400);
                    images.put(imageName, baos.toByteArray());
                }
            }
            if (content != null) {
                return content;
            }
            throw new IOException("Missing content in document");
        } catch (ZipException e) {
            LOG.error("getContent()", e);
            throw new IOException("Unzipping document;" + e);
        } catch (IOException e) {
            LOG.error("getContent()", e);
            throw new IOException("Unzipping document;" + e);
        } finally {
            if (zis != null) {
                try {
                    zis.close();
                } catch (IOException e) {
                    LOG.error("getContent.finally()", e);
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    LOG.error("getContent.finally()", e);
                }
            }
        }
    }

    public class OOStyleFilter extends XMLFilterImpl {

        Map<String, String> styles;
        boolean processAutomaticStyles;

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            styles = new HashMap<>();
            processAutomaticStyles = false;
        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
            styles.clear();
        }

        @Override
        public void startElement(String uri, String localName, String name,
                Attributes atts) throws SAXException {
            if ("office:automatic-styles".equals(name)) {
                processAutomaticStyles = true;
            } else if (processAutomaticStyles) {
                if ("style:style".equals(name)) {
                    String styleName = atts.getValue("style:name");
                    String styleParent = atts.getValue("style:parent-style-name");
                    if (!isEmpty(styleName) && !isEmpty(styleParent)) {
                        styles.put(styleName, styleParent);
                    }
                }
            } else {
                if ("text:p".equals(name)) {
                    String styleName = atts.getValue("text:style-name");
                    if (styles.containsKey(styleName)) {
                        AttributesImpl attributes = new AttributesImpl(atts);
                        styleName = styles.get(styleName);
                        int attIndex = attributes.getIndex("text:style-name");
                        attributes.setValue(attIndex, styleName);
                        super.startElement(uri, localName, name, attributes);
                        return;
                    }
                }
                super.startElement(uri, localName, name, atts);
            }
        }

        @Override
        public void endElement(String uri, String localName, String name)
                throws SAXException {
            if ("office:automatic-styles".equals(name)) {
                processAutomaticStyles = false;
            } else if (!processAutomaticStyles) {
                super.endElement(uri, localName, name);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            super.characters(ch, start, length);
        }

        boolean isEmpty(String value) {
            return value == null || value.length() == 0;
        }
    }

    public class OOImportFilter extends XMLFilterImpl {

        Attributes imageAttributes;
        String imageUri;
        String imageLocalName;
        String imageName;

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            imageAttributes = null;
        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
        }

        @Override
        public void startElement(String uri, String localName, String name,
                Attributes atts) throws SAXException {
            if (isLegalElement(name)) {
                super.startElement(uri, localName, name, atts);
            } else if ("draw:image".equals(name)) {
                imageAttributes = new AttributesImpl(atts);
                imageUri = uri;
                imageLocalName = localName;
                imageName = name;
                dumpAttr(imageAttributes);
            } else if ("text:h".equals(name)) {
                AttributesImpl attr = new AttributesImpl();
                attr.addAttribute("urn:oasis:names:tc:opendocument:xmlns:text:1.0", "style-name", "text:style-name", "CDATA", "Recipe");
                super.startElement("urn:oasis:names:tc:opendocument:xmlns:text:1.0", "p", "text:p", attr);
            }
        }

        @Override
        public void endElement(String uri, String localName, String name)
                throws SAXException {
            if (isLegalElement(name)) {
                super.endElement(uri, localName, name);
                if (imageAttributes != null && ("text:p".equals(name))) {
                    dumpAttr(imageAttributes);
                    AttributesImpl attr = new AttributesImpl();
                    attr.addAttribute("urn:oasis:names:tc:opendocument:xmlns:text:1.0", "style-name", "text:style-name", "CDATA", "image");
                    super.startElement("urn:oasis:names:tc:opendocument:xmlns:text:1.0", "p", "text:p", attr);
                    super.startElement(imageUri, imageLocalName, imageName, imageAttributes);
                    super.endElement(imageUri, imageLocalName, imageName);
                    super.endElement("urn:oasis:names:tc:opendocument:xmlns:text:1.0", "p", "text:p");
                    imageAttributes = null;
                }
            } else if ("text:h".equals(name)) {
                super.endElement("urn:oasis:names:tc:opendocument:xmlns:text:1.0", "p", "text:p");
            }
        }

        boolean isLegalElement(String name) {
            return ("text:p".equals(name) || "text:tab".equals(name) || "office:document-content".equals(name));
        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            super.characters(ch, start, length);
        }

        String dumpAttr(Attributes attr) {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < attr.getLength(); i++) {
                b.append(" ").append(attr.getQName(i)).append("=\"").append(attr.getValue(i)).append("\"");
            }
            String t = b.toString();
            return t;
        }
    }

}
