package org.braun.cookbook.web.ui;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

    private String pathParent;
    
    private List<RecipeShort> result;
    
    private List<String> pathParents;
    
    @Inject
    private RecipeFacade recipeFacade;

    @PostConstruct
    public void init() {
        refresh();
        Set<String> temp = result.stream().map(r -> r.getPathParent()).collect(Collectors.toSet());
        pathParents = new ArrayList<>(temp);
        pathParents.sort((o1, o2) -> o1.compareTo(o2));
    }

    public void refresh() {
        try {
            result = recipeFacade.findNews(14, pathParent);
        } catch (ConditionParseException e) {
            LOG.debug("Initialisierung failed.");
            result = Collections.emptyList();
        }
    }
    
    public List<RecipeShort> getResult() {
        return result;
    }

    public String getPathParent() {
        return pathParent;
    }

    public void setPathParent(String pathParent) {
        this.pathParent = pathParent;
    }
    
    public List<String> getSuggestionPathParent(String value) {
        final String val = value.toUpperCase();
        return pathParents.stream().filter(p -> p.toUpperCase().contains(val)).toList();
    }

}