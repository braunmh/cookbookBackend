package org.braun.cookbook.backend.crawler;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Named;
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
import org.braun.cookbook.backend.model.BackgroundJobType;
import org.braun.cookbook.backend.model.JsonFilter;
import org.braun.cookbook.backend.model.Recipe;
import org.braun.cookbook.backend.model.RecipeLd;
import static org.braun.cookbook.backend.model.RecipeLd.getRecipeFromJson;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author mbraun
 */
@Named
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class EffileeCrawler extends Crawler {

    @Override
    protected Recipe getRecipe(UrlString url) {
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url.getUrl()))
                .GET()
                .build();
        try (InputStream inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();) {
            Parser parser = new Parser();
            parser.setFeature(Parser.namespacePrefixesFeature, false);
            InputSource inputSource = new InputSource(inputStream);

            JsonFilter jsonFilter = new JsonFilter();
            jsonFilter.setParent(parser);

            jsonFilter.parse(inputSource);

            RecipeLd recipe = getRecipeFromJson(jsonFilter.getJson());
            if (recipe == null) {
                return null;
            }
            return toRecipe(recipe, "Effilee");
        } catch (SAXException | IOException | InterruptedException e) {
            LOG.error("execute failed with", e);
        }
        return null;
    }

    @Override
    protected String getPathParent() {
        return "effilee/" + currentYear();
    }

    
    
    @Override
    protected List<UrlString> getNewRecipes() {
        int count = 1;
        String baseUrl = "https://www.spiegel.de/services/sitesearch/search?segments=recipes&page_size=10&page=";
        Site firstSite = getSite(baseUrl + count);
        Set<UrlString> urls = new HashSet<>();

        urls.addAll(firstSite.entries.stream().map(s -> new UrlString(s.url)).toList());
        int numberOfPages = (firstSite.numResults % 10 == 0) ? firstSite.numResults / 10 : firstSite.numResults / 10 + 1;
        if (numberOfPages > 3 ) {
            numberOfPages = 3;
        }
        count++;
        int j = 0;
        for (int i = count; i < numberOfPages; i++) {
            Site site = getSite(baseUrl + i);
            urls.addAll(site.entries.stream().map(s -> new UrlString(s.url)).toList());
        }
        return new ArrayList<>(urls); 
    }
    
    @Override
    public BackgroundJobType getTaskName() {
        return BackgroundJobType.EffileeCrawler;
    }
    
    private Site getSite(String siteUrl) {
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(siteUrl))
                .GET()
                .build();
        try (HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
                InputStream inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();) {
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
   
}
