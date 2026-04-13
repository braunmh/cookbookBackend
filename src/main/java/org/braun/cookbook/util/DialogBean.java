package org.braun.cookbook.util;

import java.io.Serializable;
import org.primefaces.PrimeFaces;

/**
 *
 * @author mbraun
 */
public interface DialogBean extends Serializable {
    
    void onload();
    
    default void close() {
        PrimeFaces.current().dialog().closeDynamic(null);
    }
    
    default void close(String returnValue) {
        PrimeFaces.current().dialog().closeDynamic(returnValue);
    }
    
}
