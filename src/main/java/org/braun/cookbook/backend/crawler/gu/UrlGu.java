package org.braun.cookbook.backend.crawler.gu;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author mbraun
 */
public class UrlGu {
    private String url;
    private int count;
    private final Set<String> keyword;
    
    public UrlGu() {
        keyword = new HashSet<>();
    }

    public Set<String> getKeyword() {
        return keyword;
    }

    public void setKeyword(String value) {
        keyword.add(value);
    }
    
    public UrlGu keyword(String value) {
        keyword.add(value);
        return this;
    }

    public UrlGu keyword(String... values) {
        for (String value : values) {
            keyword.add(value);
        }
        return this;
    }

    public UrlGu keyword(Set<String> values) {
        keyword.addAll(values);
        return this;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public UrlGu url(String value) {
        setUrl(value);
        return this;
    }

    public UrlGu count(int value) {
        setCount(value);
        return this;
    }
    
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
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
        final UrlGu other = (UrlGu) obj;
        return Objects.equals(this.url, other.url);
    }
    
    
}
