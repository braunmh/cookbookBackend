package org.braun.cookbook.backend.model.recipeLd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author mbraun
 */
public class RecipeIngredientSection extends Parsable<RecipeIngredientSection> {
 
    private String title;
    
    private final List<String> ingredients;
    
    public RecipeIngredientSection() {
        ingredients = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public RecipeIngredientSection title(String value) {
        if (value != null) {
            title = value.trim();
        }
        return this;
    }

    public List<String> getIngredients() {
        return ingredients;
    }
    
    public RecipeIngredientSection addIngredients(String value) {
        if (value != null) {
            ingredients.addAll(Arrays.asList(value.split(", ")));
        }
        return this;
    }

    public RecipeIngredientSection addIngredients(List<String> value) {
        if (value != null) {
            ingredients.addAll(value);
        }
        return this;
    }

    @Override
    public boolean isEmpty() {
        return ingredients.isEmpty();
    }
    
}
