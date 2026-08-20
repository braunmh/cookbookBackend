package org.braun.cookbook.backend.model.recipe;

/**
 *
 * @author mbraun
 * @param <T>
 */
public abstract class ContentElement<T extends ContentElement<T>> implements StructureElement  {
    
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        if (value != null) {
            this.value = value.replace(' ', ' ');
        } else {
            this.value = value;
        }
    }

    @Override
    public boolean isEmpty() {
        return value == null || value.isBlank();
    }
    
    public abstract T value(String value);
    
    public abstract String getTagName();
    
    
}
