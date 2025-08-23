package org.braun.cookbook.backend.model.recipeLd;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import static jakarta.json.JsonValue.ValueType.ARRAY;
import static jakarta.json.JsonValue.ValueType.STRING;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.braun.cookbook.backend.model.recipeLd.Parsable.getString;

/**
 *
 * @author mbraun
 */
public class RecipeIngredient extends Parsable<RecipeIngredient> {

    private static final Logger LOG = LogManager.getLogger();

    private final List<RecipeIngredientSection> sections;

    public RecipeIngredient() {
        this.sections = new ArrayList<>();
    }

    public List<RecipeIngredientSection> getSections() {
        return sections;
    }

    public RecipeIngredient addSection(RecipeIngredientSection section) {
        sections.add(section);
        return this;
    }

    @Override
    public boolean isEmpty() {
        return sections.isEmpty();
    }

    public static RecipeIngredient parse(JsonValue in) {
        RecipeIngredient out = new RecipeIngredient();
        if (in != null) {
            switch (in.getValueType()) {
                case STRING:
                    out.getSections().add(new RecipeIngredientSection().addIngredients(getString(in)));
                    break;
                case OBJECT:
                    parseObject(in.asJsonObject(), out.getSections());
                case ARRAY:
                    parseArray(in.asJsonArray(), out.getSections());
                    break;
                default:
                    LOG.error("Unexpected JsonType {}", in.getValueType());
            }
        }
        return out;
    }

    private static void parseObject(JsonObject in, List<RecipeIngredientSection> sections) {
        for (Map.Entry<String, JsonValue> entry : in.entrySet()) {
            RecipeIngredientSection section = new RecipeIngredientSection().title(entry.getKey());
            sections.add(section);
            switch (entry.getValue().getValueType()) {
                case STRING:
                    section.addIngredients(getString(entry.getValue()));
                    break;
                case ARRAY:
                    JsonArray ja = entry.getValue().asJsonArray();
                    for (int k = 0; k < ja.size(); k++) {
                        JsonValue jao = ja.get(k);
                        if (JsonValue.ValueType.STRING == jao.getValueType()) {
                            section.addIngredients(getString(jao));
                        } else {
                            LOG.error("Unexpected ValueType within RecipeIngredient {}", jao.getValueType());
                        }
                    }
                    break;
                default:
                    LOG.error("Unexpected ValueType within RecipeIngredient {}", entry.getValue().getValueType());
            }
        }
    }

    private static void parseArray(JsonArray in, List<RecipeIngredientSection> sections) {
        if (in.isEmpty()) {
            return;
        }
        switch (in.get(0).getValueType()) {
            case STRING:
                RecipeIngredientSection section = new RecipeIngredientSection();
                sections.add(section);
                for (int i = 0; i < in.size(); i++) {
                    JsonValue jsonValue = in.get(i);
                    if (JsonValue.ValueType.STRING == jsonValue.getValueType()) {
                        section.addIngredients(getString(jsonValue));
                    } else {
                        LOG.error("Unexpected ValueType within RecipeIngredient {}", in.getValueType());
                    }
                }
                break;
            case OBJECT:
                for (int i = 0; i < in.size(); i++) {
                    JsonValue jsonValue = in.get(i);
                    if (JsonValue.ValueType.OBJECT == jsonValue.getValueType()) {
                        JsonObject jo = jsonValue.asJsonObject();
                        for (Map.Entry<String, JsonValue> entry : jo.entrySet()) {
                            section = new RecipeIngredientSection().title(entry.getKey());
                            sections.add(section);
                            switch (entry.getValue().getValueType()) {
                                case STRING:
                                    section.addIngredients(getString(entry.getValue()));
                                    break;
                                case ARRAY:
                                    JsonArray ja = entry.getValue().asJsonArray();
                                    for (int k = 0; k < ja.size(); k++) {
                                        JsonValue jao = ja.get(k);
                                        if (JsonValue.ValueType.STRING == jao.getValueType()) {
                                            section.addIngredients(getString(jao));
                                        } else {
                                            LOG.error("Unexpected ValueType within RecipeIngredient {}", jao.getValueType());
                                        }
                                    }
                                    break;
                                default:
                                    LOG.error("Unexpected ValueType within RecipeIngredient {}", entry.getValue().getValueType());
                            }
                        }
                    } else {
                        LOG.error("Unexpected ValueType within RecipeIngredient {}", in.getValueType());
                    }
                }
                break;
        }
    }
}
