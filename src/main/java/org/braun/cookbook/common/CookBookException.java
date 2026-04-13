package org.braun.cookbook.common;

import org.braun.cookbook.backend.model.BackgroundJobType;

/**
 *
 * @author mbraun
 */
public class CookBookException extends Exception {
    
    public CookBookException() {
        super();
    }
    
    public CookBookException(String msg) {
        super(msg);
    }
    
    public static class AlreadyRunningException extends CookBookException {
        
        public AlreadyRunningException(BackgroundJobType type) {
            super(type.name() + " already active");
        }
    }
    
    public static AlreadyRunningException newAlreadyRunningException(BackgroundJobType type) {
        return new AlreadyRunningException(type);
    }
}
