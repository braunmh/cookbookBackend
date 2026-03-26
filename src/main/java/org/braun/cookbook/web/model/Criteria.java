package org.braun.cookbook.web.model;

import java.io.Serializable;

/**
 *
 * @author mbraun
 */
public interface Criteria extends Serializable {
    
    default void isValid() throws ValidationException {
        
    }
    
    boolean isEmpty();

    default boolean isFilled() {
        return !isEmpty();
    }
}
