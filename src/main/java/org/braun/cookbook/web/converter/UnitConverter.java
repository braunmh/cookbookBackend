package org.braun.cookbook.web.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import org.apache.commons.lang3.StringUtils;
import org.braun.cookbook.backend.model.recipe.Units;

/**
 *
 * @author mbraun
 */
@FacesConverter(value = "unitConverter")
public class UnitConverter implements Converter<String> {

    @Override
    public String getAsObject(FacesContext fc, UIComponent uic, String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return Units.getInstance().getUnit(value);
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, String value) {
       // If the specified value is null, return a zero-length String
        if (value == null) {
            return "";
        }
        return Units.getInstance().getDescription(value);
    }
    
}
