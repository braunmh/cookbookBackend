package org.braun.cookbook.web.converter;

import jakarta.faces.convert.FacesConverter;
import java.util.List;
import org.braun.cookbook.web.model.CatRating;

/**
 *
 * @author mbraun
 */
@FacesConverter(value = "ratingConverter")
public class RatingConverter extends CatalogueConverter<CatRating> {

    @Override
    protected List<CatRating> getValues() {
        return CatRating.values;
    }

    
}
