package org.braun.cookbook.backend.model.recipe;

import org.braun.cookbook.backend.model.recipeLd.NutritionInformation;
import org.junit.jupiter.api.Test;

/**
 *
 * @author mbraun
 */
public class NutrientsTest {
    
    @Test
    public void test() {
        NutritionInformation ni = NutritionInformation.parse("Pro Portion: 460 kcal/ 1920 kJ, 48 g Kohlenhydrate, 18 g Eiweiß, 22 g Fett");
        System.out.println(ni.getCalories());
    }
}
