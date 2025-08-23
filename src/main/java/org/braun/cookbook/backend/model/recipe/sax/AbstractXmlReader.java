package org.braun.cookbook.backend.model.recipe.sax;

import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;

public abstract class AbstractXmlReader implements XMLReader {

    protected Map<String, Object> featureMap = new HashMap<>();
    protected Map<String, Object> propertyMap = new HashMap<>();
    protected EntityResolver entityResolver;
    protected ContentHandler contentHandler;
    protected ErrorHandler errorHandler;
    protected DTDHandler dtdHandler;
    public static final String CDATA = "CDATA";

    public void emptyElement(String uri, String localName, String qName, String value, AttributesImpl atts) throws SAXException {
        contentHandler.startElement(uri, localName, qName, atts);
        if (value != null) {
            contentHandler.characters(value.toCharArray(), 0, value.length());
        }
        contentHandler.endElement(uri, localName, qName);
        atts.clear();
    }

    @Override
    public boolean getFeature(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        if (featureMap.containsKey(name)) {
            return ((Boolean) featureMap.get(name));
        }
        return false;
    }

    @Override
    public void setFeature(String name, boolean value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        featureMap.put(name, value);
    }

    @Override
    public Object getProperty(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        return propertyMap.get(name);
    }

    @Override
    public void setProperty(String name, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        propertyMap.put(name, value);
    }

    @Override
    public void setEntityResolver(EntityResolver resolver) {
        entityResolver = resolver;
    }

    @Override
    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    @Override
    public void setContentHandler(ContentHandler handler) {
        contentHandler = handler;
    }

    @Override
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    @Override
    public void setErrorHandler(ErrorHandler handler) {
        errorHandler = handler;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @Override
    public void setDTDHandler(DTDHandler handler) {
        dtdHandler = handler;
    }

    @Override
    public DTDHandler getDTDHandler() {
        return dtdHandler;
    }

    @Override
    public void parse(String systemId) throws IOException, SAXException {
        parse(new InputSource(systemId));
    }

    @Override
    public abstract void parse(InputSource source)
            throws IOException, SAXException;

    protected File getFile(InputSource source) throws IOException {
        String systemid = source.getSystemId();
        if (systemid != null) {
            return new File(systemid);
        }
        return null;
    }

    protected InputStream getInputStream(InputSource source)
            throws IOException {
        InputStream input = source.getByteStream();
        if (input != null) {
            return input;
        }
        String systemid = source.getSystemId();
        if (systemid != null) {
            return new FileInputStream(systemid);
        }
        return null;
    }

    protected BufferedReader getBufferedReader(InputSource source)
            throws IOException {
        Reader reader = source.getCharacterStream();
        if (reader != null) {
            return new BufferedReader(reader);
        }
        InputStream input = getInputStream(source);
        if (input != null) {
            reader = new InputStreamReader(input);
            return new BufferedReader(reader);
        }
        return null;
    }
}
