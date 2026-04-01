package org.braun.cookbook.backend.model.recipeLd;

import jakarta.json.JsonArray;
import jakarta.json.JsonValue;
import static jakarta.json.JsonValue.ValueType.ARRAY;
import static jakarta.json.JsonValue.ValueType.NUMBER;
import static jakarta.json.JsonValue.ValueType.STRING;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.braun.cookbook.backend.model.recipeLd.Parsable.getInt;
import static org.braun.cookbook.backend.model.recipeLd.Parsable.getString;
import org.braun.cookbook.util.Constants;

/**
 *
 * @author mbraun
 */
public class Text extends Parsable<Text> {

    private final List<String> value;

    public Text() {
        value = new ArrayList<>();
    }

    public List<String> getValue() {
        return value;
    }

    public static Text parse(JsonValue in) {
        Text res = new Text();
        if (in != null) {
            switch (in.getValueType()) {
                case ARRAY:
                    JsonArray array = in.asJsonArray();
                    for (int i = 0; i < array.size(); i++) {
                        JsonValue jv = array.get(i);
                        if (JsonValue.ValueType.STRING == jv.getValueType()) {
                            res.add(getString(jv));
                        }
                    }
                    break;
                case STRING:
                    res.add(getString(in));
                    break;
                case NUMBER:
                    res.add(String.valueOf(getInt(in)));
                default:
            }
        }
        res.setEmpty(res.getValue().isEmpty());
        return res;
    }

    public static Text parseKeywords(JsonValue in) {
        Text res = new Text();
        if (in != null) {
            switch (in.getValueType()) {
                case ARRAY:
                    JsonArray array = in.asJsonArray();
                    for (int i = 0; i < array.size(); i++) {
                        JsonValue jv = array.get(i);
                        if (JsonValue.ValueType.STRING == jv.getValueType()) {
                            String value = getString(jv);
                            String[] values = value.split(Constants.Strings.KEYWORD_SPLIT);
                            for (String v : values) {
                                res.add(v.trim());
                            }
                        }
                    }
                    break;
                case STRING:
                    String value = getString(in);
                    String[] values = value.split(Constants.Strings.KEYWORD_SPLIT);
                    for (String v : values) {
                        res.add(v.trim());
                    }
                    break;
                case NUMBER:
                    res.add(String.valueOf(getInt(in)));
                default:
            }
        }
        res.setEmpty(res.getValue().isEmpty());
        return res;
    }

    @Override
    public String toJson() {
        if (value.size() == 1) {
            return toJsonValue(value.get(0));
        } else {
            return "[\"" + String.join("\",\"", value) + "\n]";
        }
    }

    public Text add(String value) {
        String[] values = value.split("\\n");
        getValue().addAll(Arrays.asList(values));
        return this;
    }
    
    public Text addAll(List<String> values) {
        getValue().addAll(values);
        return this;
    }
}
