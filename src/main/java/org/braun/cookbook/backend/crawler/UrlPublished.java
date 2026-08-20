package org.braun.cookbook.backend.crawler;

import java.time.OffsetDateTime;

/**
 *
 * @author mbraun
 */
public class UrlPublished extends UrlBase {

    OffsetDateTime offsetDateTime;

    public UrlPublished(String url) {
        super(url);
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
    
}
