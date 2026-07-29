package org.braun.cookbook.backend.crawler;

import org.braun.cookbook.backend.model.Recipe;

/**
 *
 * @author mbraun
 */
public abstract class Crawler extends CrawlerBase<String> {

    @Override
    protected String getPathParent(Recipe recipe, String url) {
        return getPathParent();
    }
    
    protected abstract String getPathParent();
    
}
