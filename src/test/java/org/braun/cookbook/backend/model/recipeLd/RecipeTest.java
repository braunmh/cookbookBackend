package org.braun.cookbook.backend.model.recipeLd;

import org.braun.cookbook.backend.model.RecipeLd;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import java.io.InputStream;
import java.io.StringWriter;
import org.braun.cookbook.backend.model.Recipe;
import org.junit.jupiter.api.Test;

/**
 *
 * @author mbraun
 */
public class RecipeTest {

    private static final String DIRECTORY = "org/braun/cookbook/backend/model/recipeLd/";

    public RecipeTest() {
    }

    @Test
    public void parseJsonTest() {

        String[] values = new String[]{"ndr.ratgeber.kochen.03.json", "ndr.ratgeber.kochen.02.json", "essen_und_trinken.json", "ndr.ratgeber.kochen.json", "lecker.de.json", "www.effilee.de.json"};
        for (String value : values) {
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(DIRECTORY + value)) {
                JsonReader reader = Json.createReader(inputStream);
                JsonStructure structure = reader.read();
                RecipeLd recipe = null;
                if (structure.getValueType() == JsonValue.ValueType.OBJECT) {
                    recipe = RecipeLd.parse(structure.asJsonObject());
                } else {
                    JsonArray array = structure.asJsonArray();
                    for (int i = 0; i < array.size(); i++) {
                        JsonValue jsonValue = array.get(i);
                        if (JsonValue.ValueType.OBJECT == jsonValue.getValueType()) {
                            JsonObject jsonObject = array.getJsonObject(i);
                            if (jsonObject.containsKey("@type") && "Recipe".equals(jsonObject.getString("@type"))) {
                                recipe = RecipeLd.parse(jsonObject);
                                break;
                            }
                        }
                    }
                }
                StringWriter writer = new StringWriter();
                recipe.toRecipe().marshall(writer);
                System.out.println(writer.toString());
            } catch (Exception e) {
                System.out.println("Error parsing " + value);
                e.printStackTrace(System.out);
            }
        }
    }

    @Test
    public void parseHtmlTest() {
        String[] values = new String[]{"ard_buffet.html", "ard_swr4.html"};
        for (String value : values) {
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(DIRECTORY + value)) {
                if (inputStream == null) {
                    continue;
                }
                RecipeLd recipe = RecipeArd.parse(inputStream);
                if (recipe != null) {
                StringWriter writer = new StringWriter();
                recipe.toRecipe().marshall(writer);
                System.out.println(writer.toString());
                }
            } catch (Exception e) {
                System.out.println("Error parsing " + value);
                e.printStackTrace(System.out);
            }
        }
    }

    @Test
    public void recipeUnmarshall() {
        try {
            Recipe recipe = Recipe.unmarshal("/data/CookBookJSF/Recipes", "/umschau/0004.xml");
            StringWriter sw = new StringWriter();
            System.out.println(recipe.getTitle() + ", rating: " + recipe.getRating());
            recipe.marshall(sw);
            System.out.println(sw);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
