package org.braun.cookbook.backend.model;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonReaderFactory;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParserFactory;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author mbraun
 */
public class VideoObjectLdJson {
    private final Set<String> keywords;
    private String headline;
    String description;
    
    public VideoObjectLdJson() {
        keywords = new HashSet<>();
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public String getHeadline() {
        return headline;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "VideoObjectLdJson{" + "keywords=" + keywords + ", headline=" + headline + ", description=" + description + '}';
    }
    
    
    public static VideoObjectLdJson parse(Reader reader) {
        Map<String,String> config = new HashMap<>();
        config.put("org.apache.johnzon.max-string-length", "262144");
        JsonReaderFactory factory = Json.createReaderFactory(config);
        factory.createReader(reader);
        return parse(factory.createReader(reader));
    }
    
    public static VideoObjectLdJson parse(InputStream inputStream) {
        Map<String,String> config = new HashMap<>();
        config.put("org.apache.johnzon.max-string-length", "262144");
        JsonReaderFactory factory = Json.createReaderFactory(config);
        return parse(factory.createReader(inputStream));
    }
    
    private static VideoObjectLdJson parse(JsonReader jsonReader) {
        JsonStructure structure = jsonReader.read();
        if (structure.getValueType() == JsonValue.ValueType.OBJECT) {
            JsonObject object = structure.asJsonObject();
            if (object.keySet().contains("@type")) {
                JsonString type = object.getJsonString("@type");
                if (type.getValueType() != JsonValue.ValueType.NULL) {
                    if ("VideoObject".equals(type.getString())) {
                        VideoObjectLdJson res = new VideoObjectLdJson();
                        res.description = getJsonString(object, "description");
                        res.headline = getJsonString(object, "headline");
                        res.keywords.addAll(getJsonArray(object, "keywords"));
                        return res;
                    }
                }
            }
        }
        return null;
    }
    
    private static String getJsonString(JsonObject object, String key) {
        if (object.keySet().contains(key)) {
            JsonString jsonString = object.getJsonString(key);
            if (jsonString.getValueType() != JsonValue.ValueType.NULL) {
                return jsonString.getString();
            }
        }
        return null;
    }
    private static Set<String> getJsonArray(JsonObject object, String key) {
        if (object.keySet().contains(key)) {
            JsonArray jsonArray = object.getJsonArray(key);
            if (jsonArray.getValueType() != JsonValue.ValueType.NULL) {
                Set<String> res = new HashSet<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonString jsonString = jsonArray.getJsonString(i);
                    if (jsonString.getValueType() != JsonValue.ValueType.NULL) {
                        res.add(jsonString.getString());
                    }
                }
                return res;
            }
        }
        return Collections.emptySet();
    }
}
