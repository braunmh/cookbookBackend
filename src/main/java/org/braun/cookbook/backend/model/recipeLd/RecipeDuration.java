package org.braun.cookbook.backend.model.recipeLd;

import jakarta.json.JsonValue;
import java.time.Duration;
import java.time.format.DateTimeParseException;

/**
 *
 * @author mbraun
 */
public class RecipeDuration extends Parsable<RecipeDuration> {
    
    private Duration value;

    public Duration getValue() {
        return value;
    }

    public void setValue(Duration value) {
        this.value = value;
    }

    public static RecipeDuration parse(JsonValue in) {
        return parse(getString(in));
    }

    public static RecipeDuration parse(String in) {
        RecipeDuration out = new RecipeDuration();
        if (in != null) {
            try {
                out.setValue(Duration.parse(in));
            } catch (DateTimeParseException e) {
                // noop
            }
        }
        out.setEmpty(out.value == null || out.value.isZero());
        return out;
    }
    
    @Override
    public String toJson() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
