package org.braun.cookbook.web.ui;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.braun.cookbook.backend.model.Keyword;
import org.braun.cookbook.backend.model.Recipe;
import org.braun.cookbook.backend.model.recipe.Category;
import org.braun.cookbook.backend.model.recipe.Description;
import org.braun.cookbook.backend.model.recipe.Ingredients;
import org.braun.cookbook.backend.model.recipe.Nutrients;
import org.braun.cookbook.backend.model.recipe.Yield;
import org.braun.cookbook.backend.process.KeywordFactory;
import org.braun.cookbook.backend.process.RecipeFacade;
import org.braun.cookbook.util.Configuration;
import org.braun.cookbook.util.DialogBean;
import org.braun.cookbook.util.DialogParameters;
import org.braun.cookbook.web.model.CatRating;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DialogFrameworkOptions;
import org.primefaces.model.file.UploadedFile;

/**
 *
 * @author mbraun
 */
@Named("recipeEditBean")
@ViewScoped
public class RecipeEditBean implements DialogBean {

    private static final Logger LOG = LogManager.getLogger();

    private Model model;
    
    private String path;
    
    @Inject
    private RecipeFacade recipeFacade;
    
    @Inject
    private ImageBean imageBean;
    
    public static void openDialog(String path, int innerWidth) {
        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
               .modal(true)
                .width("95%")
                .contentWidth("100%")
               .fitViewport(true)
               .responsive(true)
               .resizable(false)
               .draggable(false)
               .closeOnEscape(true)
               .build();
        if (innerWidth < 640) {
            options.setContentWidth(String.valueOf(innerWidth) + "px");
        }
        PrimeFaces.current().dialog().openDynamic("/recipe/editDialog", options, 
                DialogParameters.builder()
                .parameter(DialogParameters.Parameter.builder("path").add(path))
                .build());
    }
    
    @Override
    public void onload() {
        if (model == null) {
            Recipe recipe = recipeFacade.findByPath(path);
            model = Model.map(recipe);
            if (StringUtils.isNotBlank(recipe.getImageUrl())) {
                try (FileInputStream inputStream = 
                        new FileInputStream(new File(Configuration.getInstance().getContentDirectory() + "/" + recipe.getImageUrl()));
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
                    if (inputStream != null) {
                        byte[] buffer = new byte[2048];
                        int length;
                        while ((length = inputStream.read(buffer)) > -1) {
                            baos.write(buffer, 0, length);
                        }
                    }
                    imageBean.put(recipe.getId(), baos.toByteArray());
                } catch (IOException e) {
                    LOG.error(e.getMessage());
                }
            }
        }
    }

    public void delete() {
        recipeFacade.delete(model.getRecipe());
        close();
    }
    
    public void save() {
        try {
            byte[] image = imageBean.getImage(model.getRecipe().getId());
            if (image.length > 0) {
                String imageUrl = model.getRecipe().getRelativeName().replace(".xml", ".jpg");
                try (FileOutputStream fos = new FileOutputStream(new File(Configuration.getInstance().getContentDirectory() + "/" + imageUrl))) {
                    fos.write(image);
                    model.getRecipe().setImageUrl(imageUrl);
                } catch (IOException e) {
                    LOG.error("Store Image failed", e);
                }
            }
            recipeFacade.update(model.map());
            close();
        } catch (IOException e) {
            LOG.error("Update recipe " + path, e);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Update failed!"));
        }
    }

    @Override
    public void close() {
        imageBean.resest(model.recipe.getId());
        DialogBean.super.close(); 
    }
    
    public void handleUploadEvent(FileUploadEvent event) {
        UploadedFile file = event.getFile();
        if (file != null && file.getFileName() != null && file.getContent() != null && file.getContent().length > 0) {
            imageBean.put(model.recipe.getId(), file.getContent());
        }
    }

    public Model getModel() {
        return model;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    
    public List<Keyword> getSuggestionKeyword(String value) {
        return KeywordFactory.getInstance().getListByName(value);
    }
    
    public List<CatRating> getRatingValues() {
        return CatRating.values;
    }

    public static class Model implements Serializable {
        private Recipe recipe;
        private List<Keyword> keywords;
        private CatRating rating;
        private String nutrients;
        private String imageUrl;

        public Recipe map() {
            recipe.setRating((rating == null) ? null : rating.getValue());
            recipe.setNutrients(new Nutrients(nutrients));
            recipe.getCategories().getCategories().clear();
            for (Keyword k : keywords) {
                recipe.getCategories().add(new Category().name(String.valueOf(k.getId())));
            }
            return recipe;
        }
        
        public static Model map(Recipe in) {
            Model out = new Model();
            out.recipe = in;
            out.nutrients = in.getNutrients().toText();
            out.rating = CatRating.findById(in.getRating());
            out.keywords = new ArrayList<>();
            for (Category c : in.getCategories().getCategories())  {
                Keyword k = KeywordFactory.getInstance().getById(c.getName());
                if (k != null) {
                    out.keywords.add(k);
                }
            }
            return out;
        }
        
        public Recipe getRecipe() {
            return recipe;
        }
        
        public Boolean getEvaluated() {
            return recipe.isEvaluated();
        }
        
        public void setEvaluated(Boolean value) {
            recipe.setEvaluated(value);
        }
        
        public String getTitle() {
            return recipe.getTitle();
        }

        public void setTitle(String title) {
            recipe.setTitle(title);
        }

        public List<Keyword> getKeywords() {
            return keywords;
        }

        public void setKeywords(List<Keyword> keywords) {
            this.keywords = keywords;
        }

        public CatRating getRating() {
            return rating;
        }

        public void setRating(CatRating rating) {
            this.rating = rating;
        }

        public String getUrl() {
            return recipe.getSource().getUrl();
        }

        public void setUrl(String url) {
            recipe.getSource().setUrl(url);
        }

        public String getNutrients() {
            return nutrients;
        }

        public void setNutrients(String nutrients) {
            this.nutrients = nutrients;
        }

        public String getSource() {
            return recipe.getSource().getValue();
        }

        public void setSource(String source) {
            recipe.getSource().setValue(source);
        }

        public Description getDescription() {
            return recipe.getDescription();
        }

        public void setDescription(Description description) {
            recipe.setDescription(description);
        }

        public List<Ingredients> getIngredients() {
            return recipe.getIngredients();
        }

        public void setIngredients(List<Ingredients> ingredients) {
            recipe.setIngredients(ingredients);
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
        
        public Yield getYield() {
            return recipe.getYield();
        }
    }

    public RecipeFacade getRecipeFacade() {
        return recipeFacade;
    }

    public void setRecipeFacade(RecipeFacade recipeFacade) {
        this.recipeFacade = recipeFacade;
    }
}
