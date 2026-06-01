package org.braun.cookbook.web.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import org.braun.cookbook.backend.model.recipe.Nutrients;

/**
 *
 * @author mbraun
 */
@FacesConverter(value = "nutrientsConverter")
public class NutrientsConverter implements Converter<Nutrients> {

    @Override
    public Nutrients getAsObject(FacesContext fc, UIComponent uic, String value) {
        if (value == null) {
            return null;
        }
        return new Nutrients(value);
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Nutrients value) {
        if (value == null) {
            return null;
        }
        return value.toText();
    }
    
}
