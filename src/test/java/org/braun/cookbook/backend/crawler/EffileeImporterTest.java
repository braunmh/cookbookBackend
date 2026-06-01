package org.braun.cookbook.backend.crawler;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.braun.cookbook.backend.crawler.Crawler.LOG;
import org.braun.cookbook.backend.model.JobResult;
import org.braun.cookbook.backend.model.JobStatus;
import org.braun.cookbook.backend.model.Keyword;
import org.braun.cookbook.backend.model.Recipe;
import org.braun.cookbook.backend.model.recipe.Category;
import org.braun.cookbook.backend.process.BaseTest;
import org.braun.cookbook.backend.process.KeywordFactory;
import org.junit.jupiter.api.Test;

/**
 *
 * @author mbraun
 */
public class EffileeImporterTest extends BaseTest {

    @Test
    public void parseJson() throws InterruptedException {
        init();
        KeywordFactory.getInstance().refresh(getKeywordFacade().findAll());
        EffileeImporter imp = new EffileeImporter();
        imp.recipeFacade = getRecipeFacade();
        int count = 23;
        String baseUrl = "https://www.spiegel.de/services/sitesearch/search?segments=recipes&page_size=10&page=";
        Set<String> unkownKeywords = new HashSet<>();
        Site firstSite = getSite(baseUrl + count);

        getEntityManager().getTransaction().begin();
        unkownKeywords.addAll(imp.insertRecipes(firstSite.entries));
        getEntityManager().getTransaction().commit();

        int numberOfPages = (firstSite.numResults % 10 == 0) ? firstSite.numResults / 10 : firstSite.numResults / 10 + 1;
        count++;
        int j = 0;
        for (int i = count; i < numberOfPages; i++) {
//            if (j > 20) break;
            System.out.println(baseUrl + i);
            Site site = getSite(baseUrl + i);
            Thread.sleep(5000l);
            
            getEntityManager().getTransaction().begin();
            imp.insertRecipes(site.entries);
            getEntityManager().getTransaction().commit();
            j++;
        }
        for (String k : unkownKeywords) {
            System.out.println(k);
        }
    }

    Site getSite(String siteUrl) {
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(siteUrl))
                .GET()
                .build();
        try (InputStream inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();) {
            Site site = new Site();
            JsonReader reader = Json.createReader(inputStream);
            JsonStructure structure = reader.read();
            JsonObject jo = structure.asJsonObject();
            site.numResults = jo.getInt("num_results");
            JsonArray ja = jo.getJsonArray("results");
            for (JsonValue jv : ja) {
                JsonObject entry = jv.asJsonObject();
                int published = entry.getInt("publish_date", 0);
                LocalDateTime ldt = (published > 0) ? LocalDateTime.ofEpochSecond((long) published, 0, ZoneOffset.UTC) : null;
                String url = entry.getString("url", null);
                site.entries.add(new SiteEntry(ldt, url));
            }
            return site;
        } catch (IOException | InterruptedException e) {
            LOG.error("execute failed with", e);
        }
        return null;
    }

    class Site {

        int numResults;
        List<SiteEntry> entries;

        public Site() {
            entries = new ArrayList<>();
        }
    }

    class SiteEntry {

        LocalDateTime published;
        String url;

        public SiteEntry(LocalDateTime published, String url) {
            this.published = published;
            this.url = url;
        }

    }

    class EffileeImporter extends EffileeCrawler {

        public  Set<String> insertRecipes(List<SiteEntry> entries) {
            String pathParentBase = "effilee/";
            JobResult result = new JobResult().type(getTaskName()).status(JobStatus.successful);
            Set<String> unknownKeywords = new HashSet<>();
            int countRead = 0;
            int countInsert = 0;
            try {
                for (SiteEntry entry : entries) {
                    countRead++;
                    if (recipeFacade.findByUrl(entry.url) != null) {
                        continue;
                    }
                    String pathParent = pathParentBase + entry.published.getYear();
                    Recipe recipe = getRecipe(entry.url);
                    if (recipe != null) {
                        if (recipe.getSource().isEmpty()) {
                            recipe.getSource().setValue(pathParent);
                        }
                        recipe.getSource().setUrl(entry.url);
                        Set<Category> converted = new HashSet<>(recipe.getCategories().getCategories().size());
                        for (Category c : recipe.getCategories().getCategories()) {
                            Keyword k = KeywordFactory.getInstance().getByName(c.getName());
                            if (k != null) {
                                converted.add(new Category().name(String.valueOf(k.getId())));
                            } else {
                                LOG.info("For {} Keyword {} not found", entry.url, c.getName().toUpperCase());
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
            return unknownKeywords;
        }
    }
}
