package org.braun.cookbook.web.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import org.braun.cookbook.backend.mapping.DescriptionMapper;
import org.braun.cookbook.backend.model.recipe.Description;

/**
 *
 * @author mbraun
 */
@FacesConverter(value = "descriptionConverter")
public class DescriptionConverter implements Converter<Description> {

    @Override
    public Description getAsObject(FacesContext fc, UIComponent uic, String string) {
        return DescriptionMapper.getInstance().map(string);
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Description t) {
        return DescriptionMapper.getInstance().map(t);
    }

}
