package org.braun.cookbook.web.ui;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.braun.cookbook.backend.model.Keyword;
import org.braun.cookbook.backend.model.Recipe;
import org.braun.cookbook.backend.process.KeywordFactory;
import org.braun.cookbook.backend.process.RecipeFacade;
import org.braun.cookbook.util.Configuration;
import org.omnifaces.cdi.ViewScoped;
import org.xml.sax.SAXException;

/**
 *
 * @author mbraun
 */
@Named
@ViewScoped
public class RecipeDisplayBean implements Serializable {
    
    private static final Logger LOG = LogManager.getLogger();
    
    private String path;
    
    private Recipe content;
    
    @Inject
    private SessionUserBean sessionUserBean;
    
    public void onload() {
        if (content == null) {
            if (StringUtils.isNotBlank(path)) {
                try {
                    content = Recipe.unmarshal(Configuration.getInstance().getContentDirectory(), path);
                } catch (SAXException e) {
                    LOG.error("Reading Recipe {}", path);
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Fehler beim Lesen von Rezept " + path));
                    content = new Recipe();
                }
            } else {
                content = new Recipe();
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Variable path not set!"));
            }
        }
    }
    
    public void openRatingDialog(ActionEvent event) {
        RecipeRatingBean.openDialog(content.getRelativeName(), content.getRating(), sessionUserBean.getInnerWidth());
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Recipe getContent() {
        return content;
    }

    public void setContent(Recipe content) {
        this.content = content;
    }

    public String getKeywords() {
        if (content.getCategories().isEmpty()) {
            return null;
        }
        Stream<Keyword> ks = content.getCategories().getCategories().stream().map(c -> KeywordFactory.getInstance().getById(c.getName()));
        return String.join(", ", ks.filter(k -> k != null).map(k -> k.getName()).toList());
    }
}
