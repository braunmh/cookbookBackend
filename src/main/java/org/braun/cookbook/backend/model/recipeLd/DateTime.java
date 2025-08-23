package org.braun.cookbook.backend.model.recipeLd;

import jakarta.json.JsonValue;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

/**
 *
 * @author mbraun
 */
public class DateTime extends Parsable<DateTime> {

    private OffsetDateTime value;
    
    @Override
    public String toJson() {
        return (isEmpty()) ? null : value.toString();
    }
    
    public static DateTime parse(JsonValue in) {
        DateTime out = new DateTime();
        if (in != null && JsonValue.ValueType.STRING == in.getValueType()) {
            try {
                out.value = OffsetDateTime.parse(getString(in));
            } catch (DateTimeParseException e) {
                // noop
            }
        }
        out.setEmpty(out.value == null);
        return out;
    } 

    public static DateTime parse(String in) {
        DateTime out = new DateTime();
        if (in != null) {
            try {
                out.value = OffsetDateTime.parse(in);
            } catch (DateTimeParseException e) {
                // noop
            }
        }
        out.setEmpty(out.value == null);
        return out;
    }
    
    public OffsetDateTime getValue() {
        return value;
    }

    public void setValue(OffsetDateTime value) {
        this.value = value;
    }
    
}
