package org.braun.cookbook.backend.model.recipeLd;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 *
 * @author mbraun
 */
public class Person extends Parsable<Person> {

    private String name;
    
    @Override
    public String toJson() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public static Person parse(JsonValue in) {
        Person out = new Person();
        if (in != null) {
            if (JsonValue.ValueType.OBJECT == in.getValueType()) {
                parsePerson(in, out);
            } else if (JsonValue.ValueType.ARRAY == in.getValueType()) {
                JsonArray array = in.asJsonArray();
                for (int i = 0; i < array.size(); i++) {
                    parsePerson(array.get(i), out);
                    if (!out.isEmpty()) {
                        break;
                    }
                }
            }
        }
        out.setEmpty(out.getName() == null || out.getName().isBlank());
        return out;
    }

    private static void parsePerson(JsonValue in, Person out) {
        JsonObject jo = in.asJsonObject();
        if (jo.containsKey("@type")
                && ("Person".equals(jo.getString("@type")) || "Organization".equals(jo.getString("@type")))) {
            out.setName(getString(jo.get("name")));
        }
        out.setEmpty(out.getName() == null || out.getName().isBlank());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Person name(String value) {
        name = value;
        return this;
    }
}
