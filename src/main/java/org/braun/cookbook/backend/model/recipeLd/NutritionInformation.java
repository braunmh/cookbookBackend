package org.braun.cookbook.backend.model.recipeLd;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.braun.cookbook.backend.model.recipe.Nutrient;
import org.braun.cookbook.backend.model.recipe.Nutrients;

/**
 *
 * @author mbraun
 */
public class NutritionInformation extends Parsable<NutritionInformation> {
    
    private static final Logger LOG = LogManager.getLogger();

    /**
     * The number of milligrams of cholesterol.
     */
    private String cholesterolContent;
    /**
     * The number of grams of fiber.
     */
    private String fiberContent;
    /**
     * The number of grams of saturated fat.
     */
    private String saturatedFatContent;
    /**
     * The serving size, in terms of the number of volume or mass.
     */
    private String servingSize;
    /**
     * The number of milligrams of sodium.
     */
    private String sodiumContent;
    /**
     * The number of grams of sugar.
     */
    private String sugarContent;
    /**
     * The number of grams of trans fat.
     */
    private String transFatContent;
    /**
     * The number of grams of unsaturated fat.
     */
    private String unsaturatedFatContent;
    /**
     * The number of calories.
     */
    private String calories;
    /**
     * The number of grams of protein.
     */
    private String proteinContent;
    /**
     * The number of grams of fat.
     */
    private String fatContent;
    /**
     * The number of grams of carbohydrates.
     */
    private String carbohydrateContent;

    public static NutritionInformation parse(JsonValue in) {
        NutritionInformation out = new NutritionInformation();
        if (in != null) {
            if (JsonValue.ValueType.OBJECT == in.getValueType()) {
                JsonObject jo = in.asJsonObject();
                out.setCalories(getValue(jo.get("calories")));
                out.setProteinContent(getValue(jo.get("proteinContent")));
                out.setFatContent(getValue(jo.get("fatContent")));
                out.setCarbohydrateContent(getValue(jo.get("carbohydrateContent")));
                out.setUnsaturatedFatContent(getValue(jo.get("unsaturatedFatContent")));
                out.setTransFatContent(getValue(jo.get("transFatContent")));
                out.setSugarContent(getValue(jo.get("sugarContent")));
                out.setSodiumContent(getValue(jo.get("sodiumContent")));
                out.setServingSize(getValue(jo.get("servingSize")));
                out.setSaturatedFatContent(getValue(jo.get("saturatedFatContent")));
                out.setFiberContent(getValue(jo.get("fiberContent")));
                out.setCholesterolContent(getValue(jo.get("cholesterolContent")));
            } else if (JsonValue.ValueType.STRING == in.getValueType()) {
                out = parse(getString(in));
            }
        }
        return out;
    }

    public static NutritionInformation parse(String in) {
        NutritionInformation out = new NutritionInformation();
        Nutrients nutrients = new Nutrients(in);
        out.setServingSize(nutrients.getUnit());
        for (Nutrient nutrient : nutrients.getNutrients()) {
            if ("kcal".equals(nutrient.getUnit())) {
                out.setCalories(nutrient.getCount());
            } else {
                switch (getNutrientType(nutrient.getContent())) {
                    case protein:
                        out.setProteinContent(nutrient.getCount());
                        break;
                    case fat:
                        out.setFatContent(nutrient.getCount());
                        break;
                    case carbohydrate:
                        out.setCarbohydrateContent(nutrient.getCount());
                        break;
                    case unsaturatedFat:
                        out.setUnsaturatedFatContent(nutrient.getCount());
                        break;
                    case transFat:
                        out.setTransFatContent(nutrient.getCount());
                        break;
                    case sugar:
                        out.setSugarContent(nutrient.getCount());
                        break;
                    case sodium:
                        out.setSodiumContent(nutrient.getCount());
                        break;
                    case saturatedFat:
                        out.setSaturatedFatContent(nutrient.getCount());
                        break;
                    case fiber:
                        out.setFiberContent(nutrient.getCount());
                        break;
                    case cholesterol:
                        out.setCholesterolContent(nutrient.getCount());
                        break;
                    case other:
                        LOG.warn("Unknown nutrient found {}", nutrient.getContent());
                }
            }
        }
        return out;
    }
    
    static enum NutrientType {
        protein, fat, carbohydrate, unsaturatedFat, transFat, sugar, sodium
        , saturatedFat, fiber, cholesterol, other;
    }
    
    static final EnumMap<NutrientType, List<String>> trans = new EnumMap<>(NutrientType.class);
    static {
        trans.put(NutrientType.protein, Arrays.asList("eiweiß", "e"));
        trans.put(NutrientType.fat, Arrays.asList("fett"));
        trans.put(NutrientType.carbohydrate, Arrays.asList("kh", "kohlehydrate", "kohlenhydrate"));
        trans.put(NutrientType.unsaturatedFat, Arrays.asList("ungesättigte fettsäuren"));
        trans.put(NutrientType.transFat, Arrays.asList("transfettsäuren", "trans-fettsäuren"));
        trans.put(NutrientType.sugar, Arrays.asList("zucker"));
        trans.put(NutrientType.sodium, Arrays.asList("n", "natrium"));
        trans.put(NutrientType.saturatedFat, Arrays.asList("gesättigte fettsäuren"));
        trans.put(NutrientType.fiber, Arrays.asList("BS", "ballaststoffe"));
        trans.put(NutrientType.cholesterol, Arrays.asList("cholesterin"));
    }
    
    static NutrientType getNutrientType(String value) {
        if (value == null) {
            return NutrientType.other;
        }
        for (NutrientType nt : trans.keySet()) {
            if (trans.get(nt).contains(value.toLowerCase())) {
                return nt;
            }
        }
        return NutrientType.other;
    }
    
    private static String getValue(JsonValue value) {
        if (value == null) {
            return null;
        }
        switch (value.getValueType()) {
            case STRING:
                return getString(value);
            case NUMBER:
                return String.valueOf(getInt(value));
            default:
                return null;
        }
    }
    
    @Override
    public String toJson() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getCholesterolContent() {
        return cholesterolContent;
    }

    public void setCholesterolContent(String cholesterolContent) {
        this.cholesterolContent = cholesterolContent;
    }

    public String getFiberContent() {
        return fiberContent;
    }

    public void setFiberContent(String fiberContent) {
        this.fiberContent = fiberContent;
    }

    public String getSaturatedFatContent() {
        return saturatedFatContent;
    }

    public void setSaturatedFatContent(String saturatedFatContent) {
        this.saturatedFatContent = saturatedFatContent;
    }

    public String getServingSize() {
        return servingSize;
    }

    public void setServingSize(String servingSize) {
        this.servingSize = servingSize;
    }

    public String getSodiumContent() {
        return sodiumContent;
    }

    public void setSodiumContent(String sodiumContent) {
        this.sodiumContent = sodiumContent;
    }

    public String getSugarContent() {
        return sugarContent;
    }

    public void setSugarContent(String sugarContent) {
        this.sugarContent = sugarContent;
    }

    public String getTransFatContent() {
        return transFatContent;
    }

    public void setTransFatContent(String transFatContent) {
        this.transFatContent = transFatContent;
    }

    public String getUnsaturatedFatContent() {
        return unsaturatedFatContent;
    }

    public void setUnsaturatedFatContent(String unsaturatedFatContent) {
        this.unsaturatedFatContent = unsaturatedFatContent;
    }

    public String getCalories() {
        return calories;
    }

    public void setCalories(String calories) {
        this.calories = calories;
    }

    public String getProteinContent() {
        return proteinContent;
    }

    public void setProteinContent(String proteinContent) {
        this.proteinContent = proteinContent;
    }

    public String getFatContent() {
        return fatContent;
    }

    public void setFatContent(String fatContent) {
        this.fatContent = fatContent;
    }

    public String getCarbohydrateContent() {
        return carbohydrateContent;
    }

    public void setCarbohydrateContent(String carbohydrateContent) {
        this.carbohydrateContent = carbohydrateContent;
    }

}
