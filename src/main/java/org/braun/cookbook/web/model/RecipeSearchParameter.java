package org.braun.cookbook.web.model;

import java.util.ArrayList;
import java.util.List;
import org.braun.cookbook.backend.model.Keyword;
import org.braun.cookbook.backend.model.Suggestion;
import org.braun.cookbook.util.DateWrapper;

/**
 *
 * @author mbraun
 */
public class RecipeSearchParameter implements Criteria {

    private CatRating rating;
    
    private Suggestion directory;
    
    private Boolean evaluated;
    
    private List<Suggestion> content;
    
    private List<Keyword> keywords;

    private DateWrapper date;
    
    public CatRating getRating() {
        if (rating == null) {
            rating = new CatRating();
        }
        return rating;
    }

    public void setRating(CatRating rating) {
        this.rating = rating;
    }

    public Suggestion getDirectory() {
        if (directory == null) {
            directory = new Suggestion();
        }
        return directory;
    }

    public void setDirectory(Suggestion directory) {
        this.directory = directory;
    }

    public Boolean getEvaluated() {
        return evaluated;
    }

    public void setEvaluated(Boolean evaluated) {
        this.evaluated = evaluated;
    }

    public List<Suggestion> getContent() {
        if (content == null) {
            content = new ArrayList<>();
        }
        return content;
    }

    public void setContent(List<Suggestion> content) {
        this.content = content;
    }

    public List<Keyword> getKeywords() {
        if (keywords == null) {
            keywords = new ArrayList<>();
        }
        return keywords;
    }

    public void setKeywords(List<Keyword> keywords) {
        this.keywords = keywords;
    }

    public DateWrapper getDate() {
        if (date == null) {
            date = new DateWrapper();
        }
        return date;
    }

    public void setDate(DateWrapper date) {
        this.date = date;
    }

    @Override
    public boolean isEmpty() {
        return getRating().isEmpty() && (evaluated == null || evaluated == false) 
            && getDirectory().isEmpty() && (getContent().isEmpty() || getContent().stream().allMatch(c -> c == null || c.isEmpty()))
            && (getKeywords().isEmpty() || getKeywords().stream().allMatch(c -> c == null || c.isEmpty()))
            && getDate().isEmpty();
    }

    @Override
    public void isValid() throws ValidationException {
        if (isEmpty()) {
            throw new ValidationException("this", "Es muss mindestens ein Suchparameter angegeben werden.");
        }
    }

}
