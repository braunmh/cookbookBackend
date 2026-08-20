package org.braun.cookbook.backend.crawler;

import jakarta.inject.Inject;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.braun.cookbook.backend.importer.ImageUtil;
import org.braun.cookbook.backend.model.JobResult;
import org.braun.cookbook.backend.model.JobStatus;
import org.braun.cookbook.backend.model.Keyword;
import org.braun.cookbook.backend.model.Recipe;
import org.braun.cookbook.backend.model.RecipeLd;
import org.braun.cookbook.backend.model.recipe.Category;
import org.braun.cookbook.backend.model.recipeLd.DateTime;
import org.braun.cookbook.backend.process.BackgroundTask;
import org.braun.cookbook.backend.process.KeywordFactory;
import org.braun.cookbook.backend.process.RecipeFacade;

/**
 *
 * @author mbraun
 * @param <U>
 */
public abstract class CrawlerBase<U extends UrlBase> extends BackgroundTask {

    protected static final Logger LOG = LogManager.getLogger();

    @Inject
    RecipeFacade recipeFacade;

    @Override
    public JobResult doExecute() {

        JobResult result = new JobResult().type(getTaskName()).status(JobStatus.successful);
        Set<String> unknownKeywords = new HashSet<>();
        int countRead = 0;
        int countInsert = 0;
        String currentUrl = null;
        try {
            for (U url : getNewRecipes()) {
                currentUrl = url.getUrl();
                countRead++;
                if (recipeFacade.findByUrl(url.getUrl()) != null) {
                    continue;
                }
                Recipe recipe = getRecipe(url);
                if (recipe != null) {
                    String pathParent = getPathParent(recipe, url);
                    if (recipe.getSource().isEmpty()) {
                        recipe.getSource().setValue(pathParent);
                    }
                    recipe.getSource().setUrl(url.getUrl());
                    Set<Category> converted = new HashSet<>(recipe.getCategories().getCategories().size());
                    for (Category c : recipe.getCategories().getCategories()) {
                        Keyword k = KeywordFactory.getInstance().getByName(c.getName());
                        if (k != null) {
                            converted.add(new Category().name(String.valueOf(k.getId())));
                        } else {
                            LOG.info("For {} Keyword {} not found", url.getUrl(), c.getName().toUpperCase());
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
        } catch (Exception e) {
            LOG.error("Unexpected Exception {} processing {} ", e.getMessage(), currentUrl);
        }
        if (result.getStatus() == JobStatus.successful) {
            result.message("Number of Recipes added: " + countInsert);
        }
        LOG.info("{}. Number of recipes read {}, number of recipes inserted {}", getTaskName(), countRead, countInsert);
        result.information(unknownKeywords);
        return result;
    }

    protected Recipe toRecipe(RecipeLd recipeLd, String source) {
        Recipe recipe = recipeLd.toRecipe();
        if (recipeLd.getDatePublished() != null) {
            recipe.getSource().setValue(source + " " + getGermanFormatted(recipeLd.getDatePublished()));
        }
        return recipe;
    }
    
    protected String getGermanFormatted(DateTime ldt) {
        return ldt.getValue().format(getGermanDateFormatter());
    }
    
    protected DateTimeFormatter getGermanDateFormatter() {
        return DateTimeFormatter.ofPattern("dd.MM.yyyy").withLocale(Locale.GERMAN);
    }
    
    protected abstract Recipe getRecipe(U url);

    public byte[] getImage(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        
        try (HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
                InputStream inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (url.toLowerCase().endsWith(".png")) {
                BufferedImage original = ImageIO.read(inputStream);
                BufferedImage converted = new BufferedImage(
                        original.getWidth(),
                        original.getHeight(),
                        BufferedImage.TYPE_INT_RGB
                );
                converted.createGraphics().drawImage(original, 0, 0, Color.WHITE, null);
                ByteArrayOutputStream baosConverted = new ByteArrayOutputStream();
                ImageIO.write(converted, "jpg", baosConverted);
                ImageUtil.resizeToWidth(new ByteArrayInputStream(baosConverted.toByteArray()), baos, 400);
            } else {
                ImageUtil.resizeToWidth(inputStream, baos, 400);
            }
            return baos.toByteArray();
        } catch (IOException | InterruptedException e) {
            LOG.error("Reading image from " + url, e);
            return null;
        }
    }

    protected abstract String getPathParent(Recipe recipe, U url);

    protected abstract List<U> getNewRecipes();

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
