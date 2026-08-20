package org.braun.cookbook.backend.crawler;

import org.braun.cookbook.backend.model.Recipe;

/**
 *
 * @author mbraun
 */
public abstract class Crawler extends CrawlerBase<UrlString> {

    @Override
    protected String getPathParent(Recipe recipe, UrlString url) {
        return getPathParent();
    }
    
    protected abstract String getPathParent();
    
}
