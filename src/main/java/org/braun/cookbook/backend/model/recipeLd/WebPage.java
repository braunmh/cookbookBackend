package org.braun.cookbook.backend.model.recipeLd;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 *
 * @author mbraun
 */
public class WebPage extends Parsable<WebPage> {

    String id;
    
    @Override
    public String toJson() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public WebPage id(String value) {
        id = value;
        return this;
    }
    
    public static WebPage parse(JsonValue in) {
        WebPage out = new WebPage();
        if (in != null) {
            switch (in.getValueType()) {
                case STRING:
                    out.setId(getString(in));
                    break;
                case OBJECT:
                    JsonObject jo = in.asJsonObject();
                    if (jo.containsKey("@type") && "WebPage".equals(jo.getString("@type"))) {
                        out.setId(jo.getString("@id"));
                    }
                    break;
            }
        }
        return out;
    }

    @Override
    public boolean isEmpty() {
        return getId() == null || getId().isBlank();
    }
    
    
}
