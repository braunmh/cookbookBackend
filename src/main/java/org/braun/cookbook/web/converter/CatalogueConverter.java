package org.braun.cookbook.web.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import java.util.List;
import org.braun.cookbook.web.model.Catalogue;

/**
 *
 * @author mbraun
 * @param <T>
 */
public abstract class CatalogueConverter<T extends Catalogue> implements Converter<T> {

    @Override
    public T getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return emptyCatalogue();
        }
        List<T> result = getValues().stream().filter(c -> c.getName().equalsIgnoreCase(value)).toList();
        switch (result.size()) {
            case 0 -> throw new ConverterException("Der wert existiert nicht");
            case 1 -> {
                return (T) result.get(0).copy();
            }
            default -> throw new ConverterException("Der Wert ist nicht eindeutig");
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, T value) {
        if (value == null || value.getValue() == null) {
            return emptyCatalogue().getName();
        }
        return value.getName();
    }
    
    protected T emptyCatalogue() {
        return getValues().get(0);
    }
    
    protected abstract List<T> getValues();
}
