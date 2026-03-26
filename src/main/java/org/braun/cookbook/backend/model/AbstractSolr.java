package org.braun.cookbook.backend.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.apache.solr.client.solrj.beans.Field;

/**
 *
 * @author mbraun
 */
public class AbstractSolr {

    private double score;

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
    
    protected double stringToDouble(String value) {
        if (value == null) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    protected Date localDateTimeToDate(LocalDateTime date) {
        if (date == null) {
            return null;
        }
        return Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
    }

    protected LocalDateTime dateToLocalDateTime(Date date) {
        if (date == null) {
             return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
    
    protected String getGeoLocation(Double latitude, Double longitude) {
        // Eigentlich > 180 aber Werte > 176 führen zu einen Laufzeitfehler
        if (latitude == null || longitude == null 
            || latitude > 176 || latitude < -180 || longitude > 90 || longitude < -90) {
            return null;
        }
        
        return String.valueOf(latitude) + "," + String.valueOf(longitude);
    }
}
