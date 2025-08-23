package org.braun.cookbook.backend.model.recipe;

/**
 *
 * @author mbraun
 */
public interface IImage {
    
    int getWidth();
    
    int getHeight();
    
    String getUrl();
    
    default int getDifference() {
        if (getWidth() == 0 || getHeight() == 0 || getUrl() == null || getUrl().isBlank()) {
            return Integer.MIN_VALUE;
        }
        int diff = getWidth() - getHeight();
        return (diff > 0) ?  -diff : diff;
    }
}
