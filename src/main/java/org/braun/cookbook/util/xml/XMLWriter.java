package org.braun.cookbook.util.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.util.logging.*;
import org.xml.sax.XMLReader;

public class XMLWriter {

    ByteArrayOutputStream outputStream;
    static final transient Logger log = Logger.getLogger(XMLWriter.class.getName());

    public InputStream transform(XMLReader parser) throws IOException {
        try {
            outputStream = new ByteArrayOutputStream();
            SAXSource saxSource = new SAXSource(parser, null);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "no");

            StreamResult result = new StreamResult(outputStream);
            transformer.transform(saxSource, result);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (TransformerException e) {
            log.log(Level.SEVERE, "error creating XML-Document for parser="
                    + parser.getClass().getName(), e);
        }
        throw new IOException("error creating XML-Document for parser="
                + parser.getClass().getName());
    }

    /**
     * <p>
     * This will take the four pre-defined entities in XML 1.0 and convert their
     * character representation to the appropriate entity reference, suitable
     * for XML attributes.
     * </p>
     * <p>
     * The character '\'' is not encoded because attributes are quoted with '\"'
     * </p>
     *
     * @param st <code>String</code> input to escape.
     * @return <code>String</code> with escaped content.
     */
    public static String escapeAttributeEntities(String st) {
        if (st == null) {
            return "";
        }
        StringBuilder buff = new StringBuilder();
        char[] block = st.toCharArray();
        String stEntity = null;
        int i, last;

        for (i = 0, last = 0; i < block.length; i++) {
            switch (block[i]) {
                case '<' ->
                    stEntity = "&lt;";
                case '>' ->
                    stEntity = "&gt;";
                case '\"' ->
                    stEntity = "&quot;";
                case '&' ->
                    stEntity = "&amp;";
            }
            if (stEntity != null) {
                buff.append(block, last, i - last);
                buff.append(stEntity);
                stEntity = null;
                last = i + 1;
            }
        }
        if (last < block.length) {
            buff.append(block, last, i - last);
        }

        return buff.toString();
    }

    /**
     * <p>
     * This will take the three pre-defined entities in XML 1.0 (used
     * specifically in XML elements) and convert their character representation
     * to the appropriate entity reference, suitable for XML element.
     * </p>
     *
     * @param st <code>String</code> input to escape.
     * @return <code>String</code> with escaped content.
     */
    public static String escapeElementEntities(String st) {
        if (st == null) {
            return "";
        }
        StringBuilder buff = new StringBuilder();
        char[] block = st.toCharArray();
        String stEntity = null;
        int i, last;

        for (i = 0, last = 0; i < block.length; i++) {
            switch (block[i]) {
                case '<' ->
                    stEntity = "&lt;";
                case '>' ->
                    stEntity = "&gt;";
                case '&' ->
                    stEntity = "&amp;";
            }
            if (stEntity != null) {
                buff.append(block, last, i - last);
                buff.append(stEntity);
                stEntity = null;
                last = i + 1;
            }
        }
        if (last < block.length) {
            buff.append(block, last, i - last);
        }

        return buff.toString();
    }

}
