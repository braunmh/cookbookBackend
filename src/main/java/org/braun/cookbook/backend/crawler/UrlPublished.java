package org.braun.cookbook.backend.crawler;

import java.time.OffsetDateTime;

/**
 *
 * @author mbraun
 */
public class UrlPublished {

    String url;
    OffsetDateTime offsetDateTime;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public UrlPublished url(String value) {
        setUrl(value);
        return this;
    }
    public OffsetDateTime getOffsetDateTime() {
        return offsetDateTime;
    }

    public UrlPublished offsetDateTime(OffsetDateTime value) {
        setOffsetDateTime(value);
        return this;
    }
    
    public void setOffsetDateTime(OffsetDateTime offsetDateTime) {
        this.offsetDateTime = offsetDateTime;
    }

    public Integer getYear() {
        if (offsetDateTime == null) {
            return null;
        }
        return offsetDateTime.getYear();
    }
    
    @Override
    public String toString() {
        return url;
    }
    
}
