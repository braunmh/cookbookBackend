package org.braun.cookbook.web.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import java.util.List;
import org.braun.cookbook.backend.mapping.IngredientsMapper;
import org.braun.cookbook.backend.model.recipe.Ingredients;

/**
 *
 * @author mbraun
 */
@FacesConverter(value = "ingredientsConverter")
public class IngredientsConverter implements Converter<List<Ingredients>>{

    @Override
    public List<Ingredients> getAsObject(FacesContext fc, UIComponent uic, String value) {
        return IngredientsMapper.getInstance().map(value);
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, List<Ingredients> value) {
        return IngredientsMapper.getInstance().map(value);
    }
    
}
