package org.braun.cookbook.backend.model.recipe;

import java.util.ArrayList;
import java.util.List;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author mbraun
 */
public class Ingredients implements EmptyElement {

    private final List<Ingredient> ingredients;

    private String title;

    public Ingredients() {
        ingredients = new ArrayList<>();
    }

    @Override
    public boolean isEmpty() {
        return !ingredients.stream().anyMatch(i -> !i.isEmpty());
    }

    @Override
    public String getTagName() {
        return "Ingredients";
    }

    @Override
    public void toSaxStream(ContentHandler contentHandler) throws SAXException {
        if (isEmpty()) {
            return;
        }
        AttributesImpl attrs = new AttributesImpl();
        if (null != title) {
            addAttribute(attrs, "title", title);
        }
        contentHandler.startElement("", "Ingredients", "Ingredients", attrs);
        for (Ingredient ing : ingredients) {
            if (ing == null) {
                continue;
            }
            ing.toSaxStream(contentHandler);
        }
        contentHandler.endElement("", "Ingredients", "Ingredients");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Ingredients title(String value) {
        title = value;
        return this;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public Ingredients add(Ingredient value) {
        getIngredients().add(value);
        return this;
    }
}
