package org.braun.cookbook.backend.model.recipe;

import org.junit.jupiter.api.Test;

/**
 *
 * @author mbraun
 */
public class NutrientsTest {
    
    @Test
    public void test() {
        Nutrients ns = new Nutrients("Pro Portion: 660 kcal/ 2750 kJ / 46 g Kohlenhydrate, 21 g Eiweiß, 43 g Fett");
        for (Nutrient n : ns.getNutrients()) {
            System.out.println(n);
        }
    }
}
