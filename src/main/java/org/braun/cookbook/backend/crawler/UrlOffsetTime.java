package org.braun.cookbook.backend.crawler;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mbraun
 */
public class UrlOffsetTime extends UrlBase {

    private OffsetDateTime offsetDateTime;
    private final List<String> keyword;
    
    public UrlOffsetTime(String url) {
        super(url);
        keyword = new ArrayList<>();
    }
    
    public List<String> getKeyword() {
        return keyword;
    }

    public void setKeyword(String value) {
        keyword.add(value);
    }
    
    public UrlOffsetTime keyword(String value) {
        keyword.add(value);
        return this;
    }

    public OffsetDateTime getOffsetDateTime() {
        return offsetDateTime;
    }

    public UrlOffsetTime offsetDateTime(OffsetDateTime value) {
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
