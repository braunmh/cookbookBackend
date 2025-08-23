package org.braun.cookbook.backend.model.recipe;

import java.util.ArrayList;
import java.util.List;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author mbraun
 */
public class Description implements EmptyElement{
    
    private final List<StructureElement> content;

    public Description() {
        this.content = new ArrayList<>();
    }

    @Override
    public boolean isEmpty() {
        return !content.stream().anyMatch(c -> !c.isEmpty());
    }

    @Override
    public String getTagName() {
        return "Description";
    }

   @Override
   public void toSaxStream(ContentHandler contentHandler) throws SAXException {
      contentHandler.startElement("", "Description", "Description", EMPTY_ATTRIBUTES);
      for (StructureElement elem : content) {
         elem.toSaxStream(contentHandler);
      }
      contentHandler.endElement("", "Description", "Description");
   }

    public List<StructureElement> getContent() {
        return content;
    }
    
    public Description add(StructureElement element) {
        getContent().add(element);
        return this;
    }
}
