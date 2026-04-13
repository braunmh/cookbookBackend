package org.braun.cookbook.web.ui;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import org.braun.cookbook.backend.process.ConditionParseException;
import org.braun.cookbook.backend.process.RecipeFacade;
import org.braun.cookbook.util.DialogBean;
import org.braun.cookbook.util.DialogParameters;
import org.braun.cookbook.web.model.CatRating;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DialogFrameworkOptions;

/**
 *
 * @author mbraun
 */
@Named
@ViewScoped
public class RecipeRatingBean implements DialogBean {

    private String path;

    private Integer rating;
    
    private Model model;
    
    @Inject
    private RecipeFacade recipeFacade;

    public static void openDialog(String path, Integer rating, int innerWidth) {
        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
               .modal(true)
               .minHeight(400)
               .fitViewport(true)
               .responsive(true)
               .resizable(false)
               .draggable(false)
               .closeOnEscape(true)
               .build();
        if (innerWidth < 640) {
            options.setContentWidth(String.valueOf(innerWidth) + "px");
        }
        PrimeFaces.current().dialog().openDynamic("/recipe/ratingDialog", options, 
                DialogParameters.builder()
                .parameter(DialogParameters.Parameter.builder("path").add(path))
                .parameter(DialogParameters.Parameter.builder("rating").add(rating))
                .build());
    }
    
    @Override
    public void onload() {
        if (model == null) {
            model = new Model(getPath(), CatRating.findById(rating));
        }
    }
    
    public void save() {
        try {
            recipeFacade.rateRecipe(model.getPath(), model.getRating().getValue());
        } catch (ConditionParseException e) {
            // ignore
        }
        close();
    }
    
    public List<CatRating> completeRating(String query) {
        return CatRating.values.stream().filter(r -> r.getName().contains(query)).toList();
    }
    
    public class Model implements Serializable {
        
        public Model() {
            
        }

        public Model(String path, CatRating rating) {
            this.path = path;
            this.rating = rating;
        }
        
        private String path;

        private CatRating rating;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public CatRating getRating() {
            return rating;
        }

        public void setRating(CatRating rating) {
            this.rating = rating;
        }
    
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Model getModel() {
        return model;
    }
    
}
