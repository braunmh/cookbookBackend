package org.braun.cookbook.backend.model.recipeLd;

import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

/**
 *
 * @author mbraun
 */
public abstract class Parsable<P extends Parsable<P>> {
    
    private boolean empty;
    
    public void setEmpty(boolean empty) {
        this.empty = empty;
    }
    
    public boolean isEmpty() {
        return empty;
    }
    
    public boolean isFilled() {
        return !empty;
    }
    
    public String toJson() {
        return null;
    }

    protected String toJsonValue(String value) {
        return  "\"" + value + "\"";
    }
    
    public static JsonObject getJsonObject(JsonObject jsonObject, String name) {
        JsonValue jv = jsonObject.get(name); 
        if (jv == null || JsonValue.ValueType.ARRAY == jv.getValueType()) {
            return null;
        }
        return jv.asJsonObject();
    }
    
    public static String getString(JsonValue value) {
        return (value == null) ? null : ((JsonString) value).getString();
    }

    public static int getInt(JsonValue value) {
        if (value == null) {
            return 0;
        }
        if (JsonValue.ValueType.NUMBER == value.getValueType()) {
            return ((JsonNumber) value).intValue();
        } else if (JsonValue.ValueType.STRING == value.getValueType()) {
            try {
                return Integer.parseInt(getString(value));
            } catch (NumberFormatException e) {
                // noop
            }
        }
        return 0;
    }
}
