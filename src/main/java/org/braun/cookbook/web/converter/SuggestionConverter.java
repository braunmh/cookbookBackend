package org.braun.cookbook.web.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import org.braun.cookbook.backend.model.Suggestion;

/**
 *
 * @author mbraun
 */
@FacesConverter(value = "suggestionConverter")
public class SuggestionConverter implements Converter<Suggestion> {

    @Override
    public Suggestion getAsObject(FacesContext fc, UIComponent uic, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return new Suggestion(value, 1l);
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Suggestion k) {
        if (k == null) {
            return null;
        }
        return k.getName();
    }
    
}
