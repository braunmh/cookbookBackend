package org.braun.cookbook.web.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import org.braun.cookbook.backend.model.Keyword;
import org.braun.cookbook.backend.process.KeywordFactory;

/**
 *
 * @author mbraun
 */
@FacesConverter(value = "keywordConverter")
public class KeywordConverter implements Converter<Keyword> {

    @Override
    public Keyword getAsObject(FacesContext fc, UIComponent uic, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        Keyword k = KeywordFactory.getInstance().getByName(value);
        if (k == null) {
            throw new ConverterException("Stichwort ist nicht eindeutig.");
        }
        return k;
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Keyword k) {
        if (k == null) {
            return null;
        }
        return k.getName();
    }
    
}
