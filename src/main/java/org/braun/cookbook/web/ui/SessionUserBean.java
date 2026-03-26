package org.braun.cookbook.web.ui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author mbraun
 */
@Named(value = "sessionUserBean")
@SessionScoped
public class SessionUserBean implements Serializable {
    
    int innerWidth;
    int innerHeight;
    
    int imageWidth;

    public int getInnerWidth() {
        return innerWidth;
    }

    public void setInnerWidth(int innerWidth) {
        this.innerWidth = innerWidth;
    }

    public int getInnerHeight() {
        return innerHeight;
    }

    public void setInnerHeight(int innerHeight) {
        this.innerHeight = innerHeight;
    }
    
    public void setWidthHeight() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        innerWidth = getIntValue(params.getOrDefault("innerWidth", "0"));
        innerHeight = getIntValue(params.getOrDefault("innerHeight", "0"));

        imageWidth =  (innerWidth > 620) ? 600 : innerWidth -20;
    }
    
    public int getImageWidth() {
        return imageWidth;
    }
    
    private int getIntValue(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
