package org.braun.cookbook.backend.model.recipe.sax;

import java.io.IOException;
import org.braun.cookbook.backend.model.recipe.Recipe;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author mbraun
 */
public class RecipeXmlReader extends AbstractXmlReader {

    @Override
    public void parse(InputSource source) throws IOException, SAXException {

        if (source instanceof RecipeInputSource) {
            contentHandler.startDocument();
            Recipe recipe = ((RecipeInputSource) source).recipe;
            if (recipe != null) {
                recipe.toSaxStream(contentHandler);
            }
            contentHandler.endDocument();
        } else {
            throw new UnsupportedOperationException("Type of InputSource must be Recipe- / RecipesInputSource");
        }
    }

    public class RecipeInputSource extends InputSource {

        Recipe recipe;

        public RecipeInputSource(Recipe recipe) {
            this.recipe = recipe;
        }
    }

}
