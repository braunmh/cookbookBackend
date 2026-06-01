package org.braun.cookbook.backend.model;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.braun.cookbook.backend.model.recipe.Category;
import org.braun.cookbook.backend.model.recipe.ImageSorter;
import org.braun.cookbook.backend.model.recipe.Ingredient;
import org.braun.cookbook.backend.model.recipe.Ingredients;
import org.braun.cookbook.backend.model.recipe.Nutrient;
import org.braun.cookbook.backend.model.recipe.Nutrients;
import org.braun.cookbook.backend.model.recipe.Paragraph;
import org.braun.cookbook.backend.model.recipe.Yield;
import org.braun.cookbook.backend.model.recipeLd.DateTime;
import org.braun.cookbook.backend.model.recipeLd.ImageObject;
import org.braun.cookbook.backend.model.recipeLd.NutritionInformation;
import org.braun.cookbook.backend.model.recipeLd.Parsable;
import org.braun.cookbook.backend.model.recipeLd.Person;
import org.braun.cookbook.backend.model.recipeLd.RecipeDuration;
import org.braun.cookbook.backend.model.recipeLd.RecipeIngredient;
import org.braun.cookbook.backend.model.recipeLd.RecipeIngredientSection;
import org.braun.cookbook.backend.model.recipeLd.RecipeInstruction;
import org.braun.cookbook.backend.model.recipeLd.Text;
import org.braun.cookbook.backend.model.recipeLd.WebPage;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author mbraun
 */
public class RecipeLd extends Parsable<RecipeLd> {

    private Text name;

    private Text description;

    private RecipeIngredient recipeIngredient;

    private RecipeInstruction recipeInstructions;

    private Text cookingMethod;

    private Text recipeCategory;

    private Text recipeYield;

    private Text recipeCuisine;

    private List<ImageObject> image;

    private WebPage mainEntityOfPage;

    private DateTime datePublished;

    private DateTime dateModified;

    private RecipeDuration prepTime;
    private RecipeDuration cookTime;
    private RecipeDuration totalTime;
    private NutritionInformation nutrition;
    private Person author;
    private Person publisher;

    @Override
    public String toJson() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Recipe toRecipe() {
        Recipe recipe = new Recipe();
        if (image != null && !image.isEmpty()) {
            Collections.sort(image, new ImageSorter().reversed());
            recipe.imageUrl(image.get(0).getUrl()).width(image.get(0).getWidth()).height(image.get(0).getHeight());
        }
        setSource(recipe);
        setCategories(recipe);
        setYield(recipe);
        setTitle(recipe);
        setDescription(recipe);
        setIngredients(recipe);
        setNutrients(recipe);
        return recipe;
    }

    private void setNutrients(Recipe recipe) {
        if (isFilled(nutrition)) {
            if (nutrition.getServingSize() != null) {
                String s[] = nutrition.getServingSize().split(" ");
                recipe.getNutrients().setUnit(s.length == 1 ? s[0] : s[1]);
            } else {
                recipe.getNutrients().setUnit("Portion");
            }
            setNutrient(recipe.getNutrients(), nutrition.getCalories(), "Kalorien", "kcal");
            setNutrient(recipe.getNutrients(), nutrition.getCarbohydrateContent(), "KH", "g");
            setNutrient(recipe.getNutrients(), nutrition.getCholesterolContent(), "Cholesterin", "mg");
            setNutrient(recipe.getNutrients(), nutrition.getFatContent(), "Fett", "g");
            setNutrient(recipe.getNutrients(), nutrition.getFiberContent(), "Belaststoffe", "g");
            setNutrient(recipe.getNutrients(), nutrition.getProteinContent(), "Eiweiß", "g");
            setNutrient(recipe.getNutrients(), nutrition.getSaturatedFatContent(), "gesättigte Fettsäure", "g");
            setNutrient(recipe.getNutrients(), nutrition.getUnsaturatedFatContent(), "ungesättigte Fettsäure", "g");
            setNutrient(recipe.getNutrients(), nutrition.getSodiumContent(), "Natrium", "mg");
            setNutrient(recipe.getNutrients(), nutrition.getSugarContent(), "Zucker", "g");
            setNutrient(recipe.getNutrients(), nutrition.getTransFatContent(), "Trans-Fettsäuren", "g");
        }
    }

    private void setNutrient(Nutrients nutrients, String value, String name, String unit) {
        if (value != null) {
            String[] p = value.split(" ");
            nutrients.add(new Nutrient().unit(unit).count(p[0]).content(name));
        }
    }

    private void setIngredients(Recipe recipe) {
        if (isFilled(recipeIngredient)) {
            for (RecipeIngredientSection section : recipeIngredient.getSections()) {
                Ingredients ingredients = new Ingredients().title(section.getTitle());
                for (String value : section.getIngredients()) {
                    ingredients.add(Ingredient.parse(value));
                }
                recipe.addIngredients(ingredients);
            }
        }
    }

    private void setDescription(Recipe recipe) {
        String times = getTimes();
        if (!times.isBlank()) {
            recipe.getDescription().add(new Paragraph().value(times));
        }
        if (isFilled(description)) {
            description.getValue().stream().forEach(
                    d -> recipe.getDescription().add(new Paragraph().value(d.trim())));
        }
        if (isFilled(recipeInstructions)) {
            recipeInstructions.getValue().stream().forEach(
                    d -> recipe.getDescription().add(new Paragraph().value(d.trim())));
        }
    }

    private void setCategories(Recipe recipe) {
        if (isFilled(cookingMethod)) {
            cookingMethod.getValue().stream().forEach(c -> recipe.getCategories().add(new Category().name(c)));
        }
        if (isFilled(recipeCategory)) {
            recipeCategory.getValue().stream().forEach(c -> recipe.getCategories().add(new Category().name(c)));
        }
        if (isFilled(recipeCuisine)) {
            recipeCuisine.getValue().stream().forEach(c -> recipe.getCategories().add(new Category().name(c)));
        }
    }

    private void setSource(Recipe recipe) {
        if (isFilled(mainEntityOfPage)) {
            recipe.getSource().setUrl(mainEntityOfPage.getId());
        }
        StringBuilder temp = new StringBuilder();
        if (isFilled(publisher)) {
            temp.append(publisher.getName());
        }
        if (isFilled(author)) {
            temp.append(" (").append(author.getName()).append(")");
        }
        if (isFilled(datePublished)) {
            temp.append(" ").append(datePublished.getValue().toLocalDate().toString());
        }
        if (temp.length() > 0) {
            recipe.getSource().setValue(temp.toString().trim());
        }
    }

    private void setYield(Recipe recipe) {
        if (isFilled(recipeYield)) {
            String[] v = recipeYield.getValue().get(0).split(" ");
            switch (v.length) {
                case 2:
                    recipe.setYield(Yield.parse(v[0], v[1]));
                    break;
                case 1:
                    recipe.setYield(Yield.parse(v[0], "Portionen"));
                    break;
                default:
                    Optional<String> unit = Arrays.stream(v).filter(s -> isNumber(s)).findFirst();
                    if (unit.isPresent()) {
                        recipe.setYield(Yield.parse(unit.get(), "Portionen"));
                    }
            }
        }
    }

    private boolean isFilled(Parsable value) {
        return value != null && value.isFilled();
    }

    private boolean isNumber(String value) {
        for (char c : value.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private void setTitle(Recipe recipe) {
        if (isFilled(name)) {
            recipe.setTitle(name.getValue().get(0));
        }
    }

    private String getTimes() {
        StringBuilder temp = new StringBuilder();
        if (isFilled(totalTime)) {
            temp.append("Gesamtzeit: ").append(format(totalTime));
        }
        if (isFilled(cookTime)) {
            temp.append(" Kochzeit: ").append(format(cookTime));
        }
        if (isFilled(prepTime)) {
            temp.append(" Zubereitungszeit: ").append(format(prepTime));
        }
        return temp.toString().trim();
    }

    private String format(RecipeDuration duration) {
        long HH = duration.getValue().toHours();
        long MM = duration.getValue().toMinutesPart();
        long SS = duration.getValue().toSecondsPart();
        return String.format("%02d:%02d:%02d", HH, MM, SS);
    }

    public static RecipeLd parse(InputStream inputStream) throws IOException, SAXException {
        Parser parser = new Parser();
        parser.setFeature(Parser.namespacePrefixesFeature, false);
        InputSource inputSource = new InputSource(inputStream);
        JsonFilter jsonFilter = new JsonFilter();
        jsonFilter.setParent(parser);
        jsonFilter.parse(inputSource);
            
        return getRecipeFromJson(jsonFilter.getJson());
    }
    
    public static RecipeLd getRecipeFromJson(String json) {
        RecipeLd recipe = null;
        if (json != null) {
            StringReader input = new StringReader(json);
            JsonReader reader = Json.createReader(input);
            JsonStructure structure = reader.read();
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
        }
        return recipe;
    }
    
    public static RecipeLd parse(JsonObject jsonObject) {
        RecipeLd res = new RecipeLd();
        if (jsonObject != null) {
            res.setName(Text.parse(jsonObject.get("name")));
            res.setDescription(Text.parse(jsonObject.get("description")));
            res.setRecipeIngredient(RecipeIngredient.parse(jsonObject.get("recipeIngredient")));
            res.setRecipeInstructions(RecipeInstruction.parse(jsonObject.get("recipeInstructions")));
            res.setCookingMethod(Text.parseKeywords(jsonObject.get("cookingMethod")));
            res.setRecipeCategory(Text.parseKeywords(jsonObject.get("recipeCategory")));
            Text keywords = Text.parseKeywords(jsonObject.get("keywords"));
            if (keywords.isFilled()) {
                Set<String> ks = new HashSet<>();
                ks.addAll(res.getRecipeCategory().getValue());
                for (String k : keywords.getValue()) {
                    String[] ka = k.split(",");
                    for (String s : ka) {
                        ks.add(s.trim());
                    }
                }
                res.getRecipeCategory().getValue().clear();
                res.getRecipeCategory().getValue().addAll(ks);
            }
            res.setRecipeCuisine(Text.parseKeywords(jsonObject.get("recipeCuisine")));
            res.setImage(ImageObject.parse(jsonObject.get("image")));
            res.setMainEntityOfPage(WebPage.parse(jsonObject.get("mainEntityOfPage")));
            res.setDatePublished(DateTime.parse(jsonObject.get("datePublished")));
            res.setDateModified(DateTime.parse(jsonObject.get("dateModified")));
            res.setPrepTime(RecipeDuration.parse(jsonObject.get("prepTime")));
            res.setCookTime(RecipeDuration.parse(jsonObject.get("cookTime")));
            res.setTotalTime(RecipeDuration.parse(jsonObject.get("totalTime")));
            res.setNutrition(NutritionInformation.parse(jsonObject.get("nutrition")));
            res.setAuthor(Person.parse(jsonObject.get("author")));
            res.setPublisher(Person.parse(jsonObject.get("publisher")));
            res.setRecipeYield(Text.parse(jsonObject.get("recipeYield")));
        }
        res.setEmpty(res.getName().isEmpty() && res.getDescription().isEmpty()
                && res.getCookingMethod().isEmpty() && res.getRecipeCategory().isEmpty()
                && res.getRecipeCuisine().isEmpty() && res.getRecipeIngredient().isEmpty()
                && res.getRecipeInstructions().isEmpty() && res.getImage().isEmpty());
        return res;
    }

    public Person getAuthor() {
        return author;
    }

    public void setAuthor(Person author) {
        this.author = author;
    }

    public Person getPublisher() {
        return publisher;
    }

    public void setPublisher(Person publisher) {
        this.publisher = publisher;
    }

    public NutritionInformation getNutrition() {
        return nutrition;
    }

    public void setNutrition(NutritionInformation nutrition) {
        this.nutrition = nutrition;
    }

    public Text getName() {
        return name;
    }

    public void setName(Text name) {
        this.name = name;
    }

    public Text getDescription() {
        return description;
    }

    public void setDescription(Text description) {
        this.description = description;
    }

    public RecipeIngredient getRecipeIngredient() {
        if (recipeIngredient == null) {
            recipeIngredient = new RecipeIngredient();
        }
        return recipeIngredient;
    }

    public void setRecipeIngredient(RecipeIngredient recipeIngredient) {
        this.recipeIngredient = recipeIngredient;
    }

    public RecipeInstruction getRecipeInstructions() {
        return recipeInstructions;
    }

    public void setRecipeInstructions(RecipeInstruction recipeInstructions) {
        this.recipeInstructions = recipeInstructions;
    }

    public Text getCookingMethod() {
        return cookingMethod;
    }

    public void setCookingMethod(Text cookingMethod) {
        this.cookingMethod = cookingMethod;
    }

    public Text getRecipeCategory() {
        return recipeCategory;
    }

    public void setRecipeCategory(Text recipeCategory) {
        this.recipeCategory = recipeCategory;
    }

    public Text getRecipeCuisine() {
        return recipeCuisine;
    }

    public void setRecipeCuisine(Text recipeCuisine) {
        this.recipeCuisine = recipeCuisine;
    }

    public List<ImageObject> getImage() {
        return image;
    }

    public void setImage(List<ImageObject> image) {
        this.image = image;
    }

    public WebPage getMainEntityOfPage() {
        return mainEntityOfPage;
    }

    public void setMainEntityOfPage(WebPage mainEntityOfPage) {
        this.mainEntityOfPage = mainEntityOfPage;
    }

    public DateTime getDatePublished() {
        return datePublished;
    }

    public void setDatePublished(DateTime datePublished) {
        this.datePublished = datePublished;
    }

    public DateTime getDateModified() {
        return dateModified;
    }

    public void setDateModified(DateTime dateModified) {
        this.dateModified = dateModified;
    }

    public RecipeDuration getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(RecipeDuration prepTime) {
        this.prepTime = prepTime;
    }

    public RecipeDuration getCookTime() {
        return cookTime;
    }

    public void setCookTime(RecipeDuration cookTime) {
        this.cookTime = cookTime;
    }

    public RecipeDuration getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(RecipeDuration totalTime) {
        this.totalTime = totalTime;
    }

    public Text getRecipeYield() {
        return recipeYield;
    }

    public void setRecipeYield(Text recipeYield) {
        this.recipeYield = recipeYield;
    }

}
