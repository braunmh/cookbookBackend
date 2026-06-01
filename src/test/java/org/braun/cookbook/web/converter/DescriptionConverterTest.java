package org.braun.cookbook.web.converter;

import org.braun.cookbook.backend.model.recipe.Description;
import org.braun.cookbook.backend.model.recipe.Paragraph;
import org.junit.jupiter.api.Test;

/**
 *
 * @author mbraun
 */
public class DescriptionConverterTest {
    
    @Test
    public void test() {
        try {
            DescriptionConverter descriptionConverter = new DescriptionConverter();
            Description description = new Description();
            Paragraph p = new Paragraph();
            p.setValue("Ausser Spesen nichts gewesen");
            description.add(p);
            p = new Paragraph();
            p.setValue("Aber der versuch war es wert.");
            description.add(p);
            System.out.println(descriptionConverter.getAsString(null, null, description));
        } catch (Exception e) {
            e.printStackTrace(System.out);
        } 
    }
    
}
