package org.braun.cookbook.backend.crawler;

import java.util.Objects;

/**
 *
 * @author mbraun
 */
public abstract class UrlBase implements Comparable<UrlBase>{
    
    private final String url;
    
    public UrlBase(String url) {
        this.url = url;
    }

    public final String getUrl() {
        return url;
    }

    @Override
    public final int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.url);
        return hash;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UrlBase other = (UrlBase) obj;
        return Objects.equals(this.url, other.url);
    }

    @Override
    public int compareTo(UrlBase o) {
        return getUrl().compareTo(o.getUrl());
    }

    @Override
    public String toString() {
        return "UrlBase{" + "url=" + url + '}';
    }
    
}
