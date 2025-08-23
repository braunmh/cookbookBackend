package org.braun.cookbook.backend.model.recipe;

import org.braun.cookbook.backend.model.recipe.sax.SAXStreamable;

/**
 *
 * @author mbraun
 */
public interface EmptyElement extends SAXStreamable {
    
    @Override
    boolean isEmpty();
    
    String getTagName();
}
