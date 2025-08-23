package org.braun.cookbook.backend.model.recipe;

import java.util.ArrayList;
import java.util.List;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author mbraun
 */
public abstract class ListElement implements StructureElement {
    
    private final List<ListItem> listItems;
    
    public ListElement() {
        listItems = new ArrayList<>();
    }

    public List<ListItem> getListItems() {
        return listItems;
    }
    
    public void add(ListItem listItem) {
        getListItems().add(listItem);
    }

    @Override
    public boolean isEmpty() {
        return !listItems.stream().anyMatch(c -> !c.isEmpty());
    }
    
   @Override
   public void toSaxStream(ContentHandler contentHandler) throws SAXException {
      contentHandler.startElement("", getTagName(), getTagName(), EMPTY_ATTRIBUTES);
      for (ListItem listItem : listItems) {
         listItem.toSaxStream(contentHandler);
      }
      contentHandler.endElement("", getTagName(), getTagName());
   }
}
