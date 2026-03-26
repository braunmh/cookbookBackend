package org.braun.cookbook.backend.model.recipe;

import org.braun.cookbook.backend.model.Recipe;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

/**
 *
 * @author mbraun
 */
public class RecipeTest {

    String baseDir = "/data/CookBookJSF/Recipes";
    Set<String> categories = new HashSet<>();
    
    @Test
    public void categories() {
        File recipeBase = new File(baseDir);
        traverseDir(recipeBase);
        List<String> sorted = new ArrayList<>(categories.size());
        sorted.addAll(categories);
        Collections.sort(sorted);
        System.out.println("Number of Keyowrds defined: " + sorted.size());
        for (String c : sorted) {
            System.out.println(c);
        }
    }

    private void traverseDir(File directory) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                traverseDir(file);
            } else {
                if (file.getName().endsWith(".xml")) {
                    categories.addAll(getCategories(directory.getPath(), file.getName()));
                }
            }
        }
    }
    
    private List<String> getCategories(String baseDir, String fileName) {
        try {
            Recipe recipe = Recipe.unmarshal(baseDir, fileName);
            return recipe.getCategories().getCategories().stream().map(r -> r.getName()).toList();
        } catch (SAXException e) {
            return Collections.emptyList();
        }
    }
}
