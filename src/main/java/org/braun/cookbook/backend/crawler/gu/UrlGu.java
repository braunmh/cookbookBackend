package org.braun.cookbook.backend.crawler.gu;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.braun.cookbook.backend.crawler.UrlBase;

/**
 *
 * @author mbraun
 */
public class UrlGu extends UrlBase {
    private int count;
    private final Set<String> keyword;
    
    public UrlGu(String url) {
        super(url);
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
    
}
