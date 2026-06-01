package org.braun.cookbook.backend.crawler;

import jakarta.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.braun.cookbook.backend.importer.ImageUtil;
import org.braun.cookbook.backend.model.JobResult;
import org.braun.cookbook.backend.model.JobStatus;
import org.braun.cookbook.backend.model.Keyword;
import org.braun.cookbook.backend.model.Recipe;
import org.braun.cookbook.backend.model.recipe.Category;
import org.braun.cookbook.backend.process.BackgroundTask;
import org.braun.cookbook.backend.process.KeywordFactory;
import org.braun.cookbook.backend.process.RecipeFacade;

/**
 *
 * @author mbraun
 */
public abstract class Crawler extends BackgroundTask {

    protected static final Logger LOG = LogManager.getLogger();

    @Inject
    RecipeFacade recipeFacade;

    @Override
    public JobResult doExecute() {

        String pathParent = getPathParent();
        JobResult result = new JobResult().type(getTaskName()).status(JobStatus.successful);
        Set<String> unknownKeywords = new HashSet<>();
        int countRead = 0;
        int countInsert = 0;
        try {
            for (String url : getNewRecipes()) {
                countRead++;
                if (recipeFacade.findByUrl(url) != null) {
                    continue;
                }
                Recipe recipe = getRecipe(url);
                if (recipe != null) {
                    if (recipe.getSource().isEmpty()) {
                        recipe.getSource().setValue(pathParent);
                    }
                    recipe.getSource().setUrl(url);
                    Set<Category> converted = new HashSet<>(recipe.getCategories().getCategories().size());
                    for (Category c : recipe.getCategories().getCategories()) {
                        Keyword k = KeywordFactory.getInstance().getByName(c.getName());
                        if (k != null) {
                            converted.add(new Category().name(String.valueOf(k.getId())));
                        } else {
                            LOG.info("For {} Keyword {} not found", url, c.getName().toUpperCase());
                            unknownKeywords.add(c.getName().toUpperCase());
                        }
                    }
                    recipe.getCategories().getCategories().clear();
                    recipe.getCategories().getCategories().addAll(converted);
                    byte[] image = getImage(recipe.getImageUrl());
                    recipe.setId(null);
                    recipeFacade.insert(recipe, pathParent, image);
                    countInsert++;
                }
            }
        } catch (IOException e) {
            result.status(JobStatus.error).message("Ended with Exception: " + e.getMessage());
            LOG.error(e);
        }
        if (result.getStatus() == JobStatus.successful) {
            result.message("Number of Recipes added: " + countInsert);
        }
        LOG.info("{}. Number of recipes read {}, number of recipes inserted {}", getTaskName(), countRead, countInsert);
        result.information(unknownKeywords);
        return result;
    }

    protected abstract Recipe getRecipe(String url);

    protected byte[] getImage(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body()) {
            ImageUtil.resizeToWidth(inputStream, baos, 400);
            return baos.toByteArray();
        } catch (IOException | InterruptedException e) {
            LOG.error("Reading image from " + url, e);
            return null;
        }
    }

    protected abstract String getPathParent();

    protected abstract List<String> getNewRecipes();

    public void setRecipeFacade(RecipeFacade recipeFacade) {
        this.recipeFacade = recipeFacade;
    }

    protected Calendar now() {
        return Calendar.getInstance();
    }
    
    protected int currentYear() {
        return now().get(Calendar.YEAR);
    }
}
