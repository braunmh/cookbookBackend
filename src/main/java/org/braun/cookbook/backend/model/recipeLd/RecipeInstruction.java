package org.braun.cookbook.backend.model.recipeLd;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.braun.cookbook.backend.model.recipeLd.Parsable.getString;

/**
 *
 * @author mbraun
 */
public class RecipeInstruction extends Parsable<RecipeInstruction> {

    private final List<String> value;

    public RecipeInstruction() {
        value = new ArrayList<>();
    }

    public List<String> getValue() {
        return value;
    }

    public void add(String value) {
        String[] values = value.split("\\n");
        getValue().addAll(Arrays.asList(values));
    }
    
    public void addAll(List<String> values) {
        getValue().addAll(values);
    }
    
    @Override
    public String toJson() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public static RecipeInstruction parse(JsonValue in) {
        RecipeInstruction out = new RecipeInstruction();
        if (in != null) {
            switch (in.getValueType()) {
                case ARRAY:
                    JsonArray array = in.asJsonArray();
                    for (int i = 0; i < array.size(); i++) {
                        JsonValue jv = array.get(i);
                        if (JsonValue.ValueType.STRING == jv.getValueType()) {
                            out.add(getString(jv));
                        } else if (JsonValue.ValueType.OBJECT == jv.getValueType()) {
                            JsonObject jo = jv.asJsonObject();
                            if (jo.containsKey("@type") && "HowToStep".equals(jo.getString("@type"))) {
                                out.add(getString(jo.get("text")));
                            }
                        }
                    }
                    break;
                case STRING:
                    out.add(getString(in));
                    break;
            }
        }
        return out;
    }
}
