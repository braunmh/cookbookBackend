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

        String[] values = new String[]{"ndr.ratgeber.kochen.03.json", "ndr.ratgeber.kochen.02.json", "essen_und_trinken.json", "ndr.ratgeber.kochen.json", "lecker.de.json", "www.effilee.de.json", "rewe.json"};
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
    
    public void splitTest() {
        String desc = "1. Den Backofen auf 180 Grad (Umluft: 160 Grad, Gas Stufe 3) vorheizen. ,2. Die Kartoffeln gut waschen, mit der Schale auf ein Backblech geben und im vorgeheizten Backofen ca. 1 Stunde weich garen. ,3. In einer Pfanne ohne Fett die Haselnüsse rösten. Dann aus der Pfanne nehmen und abkühlen lassen.,4. Den Strunk und die äußeren Blätter vom Wirsing entfernen. Wirsing waschen, vierteln, den Strunk entfernen und die Wirsingviertel in feine Streifen schneiden. ,5. Zwiebel schälen und fein hacken. ,6. Majoran abbrausen, trocken schütteln und die Blättchen abzupfen. ,7. In einem großen flachen Topf Butterschmalz erhitzen, die Zwiebel darin anschwitzen, Wirsing dazugeben und bei kleiner Hitze langsam dämpfen. Mit Sahne ablöschen. Den Wirsing darin weich kochen. Mit Salz und Pfeffer würzen. ,8. Die gerösteten Haselnüsse fein hacken. Den Käse fein reiben.,9. Die noch sehr warmen weich gegarten Kartoffeln durchschneiden, das Kartoffelinneres herauskratzen und durch eine Kartoffelpresse drücken. ,10. Die Eier trennen. ,11. Das Püree mit Salz und Muskatnuss würzen. Eigelb und Kartoffelstärke darunter mischen. ,12. Diesen Kartoffelteig mit dem Nudelholz etwa 1 cm dünn ausrollen (das Nudelholz sollte immer mit Kartoffelstärke bestäubt sein, damit der Teig nicht klebt). Aus der Teigplatte lange Dreiecke ausschneiden. Majoran, gehackte Nüsse und Käse darauf streuen und von der breiten Seite her aufrollen. Die Enden zu einem Croissant umbiegen.,13. Die Kartoffelcroissants auf ein mit Backpapier ausgelegtes Backblech legen und mit Eiweiß bestreichen. Im vorgeheizten Ofen ca. 7 Minuten backen. ,14. Kartoffelcroissants und Rahmwirsing auf Teller verteilen und servieren. ";
        String[] vs = desc.split("( ,)|(\\.,)");
        for (String v : vs) {
            System.out.println(v);
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
