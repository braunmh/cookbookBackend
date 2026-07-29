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
import org.braun.cookbook.common.EndOfProcessing;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 *
 * @author mbraun
 */
@Named
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class EffileeCrawler extends Crawler {

    @Override
    protected Recipe getRecipe(String url) {
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
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
    protected List<String> getNewRecipes() {
        int count = 1;
        String baseUrl = "https://www.spiegel.de/services/sitesearch/search?segments=recipes&page_size=10&page=";
        Site firstSite = getSite(baseUrl + count);
        Set<String> urls = new HashSet<>();

        urls.addAll(firstSite.entries.stream().map(s -> s.url).toList());
        int numberOfPages = (firstSite.numResults % 10 == 0) ? firstSite.numResults / 10 : firstSite.numResults / 10 + 1;
        if (numberOfPages > 3 ) {
            numberOfPages = 3;
        }
        count++;
        int j = 0;
        for (int i = count; i < numberOfPages; i++) {
            Site site = getSite(baseUrl + i);
            urls.addAll(site.entries.stream().map(s -> s.url).toList());
        }
        return new ArrayList<>(urls); 
    }
    
    protected List<String> getNewRecipesFormMainSite() {
        String prefix = "https://www.spiegel.de/effilee";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(prefix))
                .GET()
                .build();
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        OverviewFilter overviewFilter = new OverviewFilter(prefix);
        try (InputStream inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();) {
            Parser reader = new Parser();
            reader.setFeature(Parser.namespacePrefixesFeature, false);
            InputSource inputSource = new InputSource(inputStream);
            overviewFilter.setParent(reader);
            overviewFilter.parse(inputSource);
        } catch (EndOfProcessing e) {
            // Just ignore
        } catch (SAXException | IOException | InterruptedException e) {
            LOG.error("execute failed with", e);
        }
        return overviewFilter.getUrls();
    }

    @Override
    public BackgroundJobType getTaskName() {
        return BackgroundJobType.EffileeCrawler;
    }
    
    class OverviewFilter extends XMLFilterImpl {
        
        enum Step {
            other, section, header, title, finished;
        }
        private final String prefix;
        private Step step;
        
        Set<String> urls;
        
        public OverviewFilter(String prefix) {
            urls = new HashSet<>();
            step = Step.other;
            this.prefix = prefix;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            switch (step) {
                case other -> {
                    if ("section".equals(localName) && "block>recipeslider".equals(atts.getValue("data-area"))) {
                        step = Step.section;
                    }
                }
                case section -> {
                    if ("header".equals(localName)) {
                        step = Step.title;
                    }
                }
                case title -> {
                    if ("a".equals(localName)) {
                        String url = atts.getValue("href");
                        if (url != null) {
                            urls.add(((url.indexOf("://") > 0)) ? url : prefix + url);
                        }
                    }
                }
            }
            super.startElement(uri, localName, qName, atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (step) {
                case section, header -> {
                    if ("section".equals(localName)) {
                        step = Step.other;
                    }
                }
                case title -> {
                    if ("header".equals(localName)) {
                        step = Step.section;
                    }
                }
            }
        }

        public List<String> getUrls() {
            return new ArrayList<>(urls);
        }
        
    }
    public Site getSite(String siteUrl) {
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
   
}
