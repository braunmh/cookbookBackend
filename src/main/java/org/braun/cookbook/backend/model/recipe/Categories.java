package org.braun.cookbook.backend.model.recipe;

import java.util.ArrayList;
import java.util.List;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author mbraun
 */
public class Categories implements EmptyElement {

    private final List<Category> categories;
    
    public Categories() {
        categories = new ArrayList<>();
    }
    @Override
    public boolean isEmpty() {
        return !categories.stream().anyMatch(c -> !c.isEmpty());
    }

    @Override
    public String getTagName() {
        return "Categories";
    }

   @Override
   public void toSaxStream(ContentHandler contentHandler) throws SAXException {
      contentHandler.startElement("", "Categories", "Categories", EMPTY_ATTRIBUTES);
      if (!isEmpty()) {
         for (Category c : categories) {
            c.toSaxStream(contentHandler);
         }
      }
      contentHandler.endElement("", "Categories", "Categories");
   }
    public List<Category> getCategories() {
        return categories;
    }

    public Categories add(Category value) {
        categories.add(value);
        return this;
    }
}
