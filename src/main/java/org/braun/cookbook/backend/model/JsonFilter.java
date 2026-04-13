package org.braun.cookbook.backend.model;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import java.io.CharArrayWriter;
import java.io.StringReader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 *
 * @author mbraun
 */
public class JsonFilter extends XMLFilterImpl {
    
    String json;
    CharArrayWriter writer = new CharArrayWriter();
    boolean write = false;
    boolean done = false;
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (!done) {
            if ("script".equals(localName) && "application/ld+json".equals(atts.getValue("type"))) {
                write = true;
            }
        }
        super.startElement(uri, localName, qName, atts);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (write && "script".equals(localName)) {
            write = false;
            StringReader reader = new StringReader(writer.toString());
            JsonReader jsonReader = Json.createReader(reader);
            JsonStructure structure = jsonReader.read();
            switch(structure.getValueType()) {
                case OBJECT -> {
                    JsonObject object = structure.asJsonObject();
                    if (object.keySet().contains("@type")) {
                        JsonString type = object.getJsonString("@type");
                        if (type.getValueType() != JsonValue.ValueType.NULL) {
                            if ("Recipe".equals(type.getString())) {
                                json = writer.toString();
                                done = true;
                            }
                        }
                    }
                }
                case ARRAY -> {
                    JsonArray array = structure.asJsonArray();
                    for (int i = 0; i < array.size(); i++) {
                        JsonValue jsonValue = array.get(i);
                        if (JsonValue.ValueType.OBJECT == jsonValue.getValueType()) {
                            JsonObject jsonObject = array.getJsonObject(i);
                            if (jsonObject.containsKey("@type") && "Recipe".equals(jsonObject.getString("@type"))) {
                                json = writer.toString();
                                done = true;
                            }
                        }
                    }
                }
            }
            writer.reset();
        }
        super.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (write) {
            writer.write(ch, start, length);
        }
        super.characters(ch, start, length);
    }

    public String getJson() {
        return json;
    }
    
}
