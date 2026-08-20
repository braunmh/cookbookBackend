package org.braun.cookbook.backend.crawler;

import java.util.Objects;

/**
 *
 * @author mbraun
 */
public class UrlAuthor extends UrlBase {
    
    private String author;
    
    public UrlAuthor(String url) {
        super(url);
    }
    
    public UrlAuthor author(String value) {
        author = value;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public String toString() {
        return "UrlAuthor{url = " + getUrl() + ", author=" + author + '}';
    }

    
}
