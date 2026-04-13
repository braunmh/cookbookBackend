package org.braun.cookbook.backend.mapping;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.junit.jupiter.api.Test;

/**
 *
 * @author mbraun
 */
public class JobMapperTest {

    @Test
    public void listToString() {
        List<String> values = List.of("element1", "element2");
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = Json.createWriter(writer); 
        JsonArray array = Json.createArrayBuilder(values).build();
        jsonWriter.writeArray(array);
        System.out.println(writer.toString());
        
        StringReader reader = new StringReader(writer.toString());
        JsonReader jsonReader = Json.createReader(reader);
        array = jsonReader.readArray();
        List<String> res = array.stream()
                .filter(j -> j.getValueType() == JsonValue.ValueType.STRING)
                .map(j -> j.toString()).toList();
        System.out.println(res);
    }
}
