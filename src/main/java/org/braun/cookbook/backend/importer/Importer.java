package org.braun.cookbook.backend.importer;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.lang3.StringUtils;
import org.braun.cookbook.backend.process.RecipeFacade;

/**
 *
 * @author mbraun
 */
public abstract class Importer {

    private final RecipeFacade recipeFacade;
    private final String pathParent;
    private final InputStream inputStream;

    public Importer(RecipeFacade recipeFacade, InputStream inputStream, String pathParent) {
        this.recipeFacade = recipeFacade;
        this.inputStream = inputStream;
        this.pathParent = pathParent;
    }
    
    protected boolean recipeExists(String url) throws IOException {
        if (StringUtils.isBlank(url)) {
            return false;
        }

        return recipeFacade.findByUrl(url) != null;
    }

    public RecipeFacade getRecipeFacade() {
        return recipeFacade;
    }

    public String getPathParent() {
        return pathParent;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
