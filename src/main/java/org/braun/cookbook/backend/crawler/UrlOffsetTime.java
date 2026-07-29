package org.braun.cookbook.backend.crawler;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author mbraun
 */
public class UrlOffsetTime {
    private String url;
    private OffsetDateTime offsetDateTime;
    private final List<String> keyword;
    
    public UrlOffsetTime() {
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public UrlOffsetTime url(String value) {
        setUrl(value);
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
    
    @Override
    public String toString() {
        return url;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.url);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UrlOffsetTime other = (UrlOffsetTime) obj;
        return Objects.equals(this.url, other.url);
    }
    
    
}
