package org.braun.cookbook.web.ui;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mbraun
 */
@Named
@SessionScoped
public class ImageBean implements Serializable {
    
    public static final String PREFIX = "tEmPImAgE/";
    
    private Map<String, byte[]> images;
    
    @PostConstruct
    public void postConstruct() {
        images = new HashMap<>();
    }
    
    public byte[] getImage(String id) {
        if (images.containsKey(id)) {
            return images.get(id);
        } else {
            return new byte[0];
        }
    }
    
    public void put(String id, byte[] bytes) {
        images.put(id, bytes);
    }
    
    public void resest(String id) {
        images.remove(id);
    }
    
    public String getPrefix() {
        return PREFIX;
    }
}
