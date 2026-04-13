package org.braun.cookbook.web.ui;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.braun.cookbook.backend.model.RecipeShort;
import org.braun.cookbook.backend.process.ConditionParseException;
import org.braun.cookbook.backend.process.RecipeFacade;
import org.omnifaces.cdi.ViewScoped;

/**
 * Search for recipes added or modified between the last 2 weeks 
 *
 *
 * @author mbraun
 */
@Named
@ViewScoped
public class NewsBean implements Serializable {
    
    private static final Logger LOG = LogManager.getLogger();

    private List<RecipeShort> result;
    
    @Inject
    private RecipeFacade recipeFacade;

    @PostConstruct
    public void init() {
        try {
            result = recipeFacade.findNews(14);
        } catch (ConditionParseException e) {
            LOG.debug("Initialisierung failed.");
            result = Collections.emptyList();
        }
    }

    public List<RecipeShort> getResult() {
        return result;
    }
    
}
