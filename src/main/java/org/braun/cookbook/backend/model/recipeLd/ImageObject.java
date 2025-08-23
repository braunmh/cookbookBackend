package org.braun.cookbook.backend.model.recipeLd;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import java.util.ArrayList;
import java.util.List;
import org.braun.cookbook.backend.model.recipe.IImage;

/**
 *
 * @author mbraun
 */
public class ImageObject extends Parsable<ImageObject> implements IImage {
    
    private String name;
    
    private String url;
    
    private int width;
    
    private int height;
        
    @Override
    public String toJson() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public static List<ImageObject> parse(JsonValue in) {
        List<ImageObject> res = new ArrayList<>();
        if (in != null) {
            switch (in.getValueType()) {
                case ARRAY:
                    JsonArray array = in.asJsonArray();
                    for (int i = 0; i < array.size(); i++) {
                        ImageObject io = parseObject(array.get(i));
                        if (!io.isEmpty()) {
                            res.add(io);
                        }
                    }
                    break;
                case STRING:
                    ImageObject io = parseObject(in);
                    if (!io.isEmpty()) {
                        res.add(io);
                    }
                    break;
                case OBJECT:
                    io = parseObject(in);
                    if (!io.isEmpty()) {
                        res.add(io);
                    }
                    break;
            }
        }
        return res;
    }

    private static ImageObject parseObject(JsonValue in) {
        ImageObject out = new ImageObject();
        if (in != null) {
            switch (in.getValueType()) {
                case STRING:
                    out.setUrl(getString(in));
                    break;
                case OBJECT:
                    JsonObject jo = in.asJsonObject();
                    if (jo.containsKey("@type") && "ImageObject".equals(jo.getString("@type"))) {
                        out.setName(getString(jo.get("name")));
                        out.setUrl(getString(jo.get("url")));
                        out.setHeight(getInt(jo.get("height")));
                        out.setWidth(getInt(jo.get("width")));
                    }
                    break;
            }
            out.setEmpty(out.getUrl() == null || out.getUrl().isBlank());
        }
        return out;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ImageObject name(String value) {
        name = value;
        return this;
    }
    
    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ImageObject url(String value) {
        url = value;
        return this;
    }
    
    @Override
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public ImageObject width(int value) {
        width = value;
        return this;
    }
    
    @Override
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
    public ImageObject height(int value) {
        height = value;
        return this;
    }
    
}
