package org.braun.cookbook.util;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;

/**
 *
 * @author mbraun
 */
public class SolrQueryBuilder {
    
    private final SolrQuery solrQuery;
    private final StringBuilder query;
    
    public SolrQueryBuilder() {
        solrQuery = new SolrQuery();
        solrQuery.setStart(0);
        solrQuery.setRows(2000);
        query = new StringBuilder();
    }
    
    public SolrQuery build() {
        solrQuery.setQuery(query.toString());
        return solrQuery;
    }
    
    public SolrQueryBuilder addQuery(String fieldName, DateWrapper value) {
        if (value.isEmpty()) {
            return this;
        }
        addOperator();
        query.append(fieldName).append(":[")
            .append(value.getLowerBoundZdtFormatted())
            .append(" TO ")
            .append(value.getUpperBoundZdtFormatted())
            .append("]");
        return this;
    }
    
    public SolrQueryBuilder addQuery(String fieldName, Integer from, Integer to) {
        if (from == null && to == null) {
            return this;
        }
        if (from == null) {
            return addQuery(fieldName, to);
        }
        if (to == null) {
            return addQuery(fieldName, from);
        }
        if (Objects.equals(from, to)) {
            return addQuery(fieldName, from);
        }
        addOperator();
        query.append(fieldName).append(":[")
            .append(String.valueOf(from))
            .append(" TO ")
            .append(String.valueOf(to))
            .append("]");
        return this;
    }
    
    public SolrQueryBuilder addQuery(String fieldName, Double from, Double to) {
        if (from == null && to == null) {
            return this;
        }
        if (from == null) {
            return addQuery(fieldName, to);
        }
        if (to == null) {
            return addQuery(fieldName, from);
        }
        if (Objects.equals(from, to)) {
            return addQuery(fieldName, from);
        }
        addOperator();
        query.append(fieldName).append(":[")
            .append(String.valueOf(from))
            .append(" TO ")
            .append(String.valueOf(to))
            .append("]");
        return this;
    }
    
    public SolrQueryBuilder addQuery(String fieldName, DateWrapper from, DateWrapper to) {
        if (from.isEmpty() && to.isEmpty()) {
            return this;
        }
        if (from.isEmpty()) {
            return addQuery(fieldName, to);
        }
        if (to.isEmpty()) {
            return addQuery(fieldName, from);
        }
        if (from == to) {
            return addQuery(fieldName, from);
        }
        addOperator();
        query.append(fieldName).append(":[")
            .append(escapeSpecialCharacters(from.getLowerBoundZdtFormatted()))
            .append(" TO ")
            .append(escapeSpecialCharacters(to.getUpperBoundZdtFormatted()))
            .append("]");
        return this;
    }
    
    public SolrQueryBuilder addQuery(String fieldName, Boolean value) {
        if (value == null) {
            return this;
        }
        addOperator();
        query.append(fieldName).append(":")
                .append(String.valueOf(value));
        return this;
    }
    
    public SolrQueryBuilder addQueryString(String fieldName, String value) {
        if (StringUtils.isEmpty(value)) {
            return this;
        }
        addOperator();
        value = escapeSpecialCharacters(value);
        query.append(fieldName).append(":")
            .append(value);
        return this;
    }
    
    public SolrQueryBuilder addQueryText(String fieldName, String value) {
        if (StringUtils.isEmpty(value)) {
            return this;
        }
        addOperator();
        value = escapeSpecialCharacters(value);
        String [] words = value.split(" ");
        StringBuilder tmp = new StringBuilder(words[0]);
        for (int i = 1; i < words.length; i++) {
            if (words[i].equals("")) {
                continue;
            }
            tmp.append(" AND ").append(words[i]);
        }
        query.append(fieldName)
            .append(":")
            .append("(")
            .append(tmp)
            .append(")");
        return this;
    }
    
    public SolrQueryBuilder addQuery(String fieldName, Integer value) {
        if (value == null) {
            return this;
        }
        addOperator();
        query.append(fieldName).append(":")
            .append(String.valueOf(value));
        return this;
    }
    
    public SolrQueryBuilder addQuery(String fieldName, Double value) {
        if (value == null) {
            return this;
        }
        addOperator();
        query.append(fieldName).append(":")
            .append(String.valueOf(value));
        return this;
    }
    
    public SolrQueryBuilder addQueryLong(String fieldName, Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return this;
        }
        List<String> values = ids.stream().map(i -> String.valueOf(i)).collect(Collectors.toList());
        addOperator();
        query.append(fieldName).append(":")
            .append("(")
            .append(String.join(" ", values))
            .append(")");
        return this;
    }
    
    public SolrQueryBuilder addQueryInt(String fieldName, Collection<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return this;
        }
        List<String> values = ids.stream().map(i -> String.valueOf(i)).collect(Collectors.toList());
        addOperator();
        query.append(fieldName).append(":")
            .append("(")
            .append(String.join(" ", values))
            .append(")");
        return this;
    }
    
    public SolrQueryBuilder addQuery(String fieldName, Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return this;
        }
        addOperator();
        query.append(fieldName).append(":")
            .append("(")
            .append(String.join(" ", ids))
            .append(")");
        return this;
    }
    
    /**
     * Adds an Geo-Search
     * 
     * @param fieldName
     * @param latitude
     * @param longitude
     * @param distance
     * @return SolrQueryBuilder
     */
    public SolrQueryBuilder addQuery(String fieldName, Double latitude, Double longitude, Double distance) {
        if (latitude == null || longitude == null || distance == null) {
            return this;
        }
        solrQuery.setFilterQueries("{!geofilt}");
        solrQuery.set("pt", String.valueOf(latitude) + "," + String.valueOf(longitude));
        solrQuery.set("sfield", fieldName);
        solrQuery.set("d", String.valueOf(distance));
        return this;
    }
    
    /**
     * 
     * @param fieldName to include in result
     * @return SolrQueryBuilder
     */
    public SolrQueryBuilder addField(String fieldName) {
        solrQuery.addField(fieldName);
        return this;
    }
    
    public SolrQueryBuilder setStart(int start) {
        solrQuery.setStart(start);
        return this;
    }

    /**
     * 
     * @param rows number of rows to return
     * @return SolrQueryBuilder
     */
    public SolrQueryBuilder setRows(int rows) {
        solrQuery.setRows(rows);
        return this;
    }
    
    private void addOperator() {
        if (query.length() > 0) {
            query.append(" AND ");
        }
    }
    
    /**
     * Escape these list + - && || ! ( ) { } [ ] ^ " ~ * ? : \ / of characters
     * @param value to escape
     * @return the escaped value
     */
    private String escapeSpecialCharacters(String value) {
        int outLength = 0;
        char[] out = new char[value.length() * 2];
        for (char c : value.toCharArray()) {
            if ("+-!(){}[]^\"~*?:\\/".indexOf(c) > -1) {
                out[outLength++] = '\\';
            }
            out[outLength++] = c;
        }
        return new String(out, 0, outLength);
    }
}
