package org.braun.cookbook.web.ui;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import org.braun.cookbook.backend.model.Keyword;
import org.braun.cookbook.backend.model.RecipeShort;
import org.braun.cookbook.backend.model.RecipeSolr;
import org.braun.cookbook.backend.model.Suggestion;
import org.braun.cookbook.backend.process.ConditionParseException;
import org.braun.cookbook.backend.process.KeywordFactory;
import org.braun.cookbook.backend.process.RecipeFacade;
import org.braun.cookbook.web.model.CatRating;
import org.braun.cookbook.web.model.RecipeSearchParameter;
import org.braun.cookbook.web.model.ValidationException;
import org.omnifaces.cdi.ViewScoped;

/**
 *
 * @author mbraun
 */
@Named
@ViewScoped
public class RecipeSearchBean implements Serializable {
    
    private RecipeSearchParameter parameter;
    
    private List<RecipeShort> result;
    
    @Inject
    private SessionUserBean sessionUserBean;
    
    @Inject
    private RecipeFacade recipeFacade;
    
    @PostConstruct
    public void init() {
        parameter = new RecipeSearchParameter();
    }
    
    public String reset() {
        init();
        return null;
    }
    
    public String execute() {
        try {
            getParameter().isValid();
            result = recipeFacade.searchByAttributes(
                getParameter().getContent().isEmpty()
                    ? null
                    : String.join(" ", getParameter().getContent().stream().map(c -> c.getName()).toList()), 
                 getParameter().getKeywords().isEmpty()
                         ? null
                    : getParameter().getKeywords().stream().map(k -> k.getId()).toList(), 
                getParameter().getRating().getValue(), 
                getParameter().getDirectory().getName(), 
                getParameter().getEvaluated(), 
                getParameter().getDate().getUncompleteDateTime().toString(), // dateFrom
                null  // dateTo
            );
        } catch (ValidationException e) {
            FacesContext.getCurrentInstance().addMessage(e.getFieldName("parameter"), e.toFacesMessage());
        } catch (ConditionParseException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
        }
        return null;
    }
    
    public List<Suggestion> getSuggestionContent(String value) {
        return recipeFacade.getSuggestion(RecipeSolr.FIELD_CONTENT_SUGGEST, value);
    }

    public List<Suggestion> getSuggestionPathParent(String value) {
        return recipeFacade.getSuggestion(RecipeSolr.FIELD_PATH_PARENT, value);
    }

    public List<Keyword> getSuggestionKeyword(String value) {
        return KeywordFactory.getInstance().getListByName(value);
    }
    
    public RecipeSearchParameter getParameter() {
        return parameter;
    }

    public List<RecipeShort> getResult() {
        return result;
    }

    public List<CatRating> getRatingValues() {
        return CatRating.values;
    }
    
   
}
