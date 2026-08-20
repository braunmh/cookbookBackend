package org.braun.cookbook.backend.crawler;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.braun.cookbook.backend.crawler.CrawlerBase.LOG;
import org.braun.cookbook.backend.model.BackgroundJobType;
import org.braun.cookbook.backend.model.JsonFilter;
import org.braun.cookbook.backend.model.Recipe;
import org.braun.cookbook.backend.model.RecipeLd;
import static org.braun.cookbook.backend.model.RecipeLd.getRecipeFromJson;
import org.braun.cookbook.backend.model.recipeLd.Text;
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
@Named("brigitteCrawler")
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class BrigitteCrawler extends Crawler {

    @Override
    protected String getPathParent(Recipe recipe, UrlString url) {
        LocalDateTime ldt = (null != recipe.getPublished() && recipe.getPublished() > 0)
                ? LocalDateTime.ofEpochSecond(recipe.getPublished() / 1000, 0, ZoneOffset.UTC)
                : LocalDateTime.now();
        return "brigitte/" + ldt.getYear();
    }

    @Override
    protected String getPathParent() {
        Calendar now = Calendar.getInstance();
        return "brigitte/" + now.get(Calendar.YEAR);
    }

    @Override
    protected Recipe getRecipe(UrlString url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url.getUrl()))
                .GET()
                .build();
        try (HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
            InputStream inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();) {
            Parser parser = new Parser();
            parser.setFeature(Parser.namespacePrefixesFeature, false);
            InputSource inputSource = new InputSource(inputStream);

            JsonFilter jsonFilter = new JsonFilter();
            jsonFilter.setParent(parser);

            KeywordFilter keywordFilter = new KeywordFilter();
            keywordFilter.setParent(jsonFilter);
            keywordFilter.parse(inputSource);

            RecipeLd recipeLd = getRecipeFromJson(jsonFilter.getJson());
            if (recipeLd == null) {
                return null;
            }
            recipeLd.setRecipeCategory(new Text().addAll(keywordFilter.getKeywords()));
            return toRecipe(recipeLd, "Brigitte");
        } catch (SAXException | IOException | InterruptedException e) {
            LOG.error("execute failed with", e);
        }
        return null;
    }

    @Override
    protected List<UrlString> getNewRecipes() {
        String prefix =  "https://www.brigitte.de/rezepte/";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(prefix))
                .GET()
                .build();
        NewRecipesFilter newRecipesFilter = new NewRecipesFilter();
        try (HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
            InputStream inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();) {
            if (inputStream != null) {
                Parser reader = new Parser();
                reader.setFeature(Parser.namespacePrefixesFeature, false);
                InputSource inputSource = new InputSource(inputStream);
                newRecipesFilter.setParent(reader);
                newRecipesFilter.parse(inputSource);
            }
        } catch (EndOfProcessing e) {
            // ignore
        } catch (SAXException | IOException | InterruptedException e) {
            LOG.error("execute failed with", e);
        }
        return new ArrayList<>(newRecipesFilter.getUrls());
    }

    @Override
    public BackgroundJobType getTaskName() {
        return BackgroundJobType.BrigitteCrawler;
    }
    
    class NewRecipesFilter extends XMLFilterImpl {

        private final Set<UrlString> urls;
        
        private int stack = 0;
        String url;
        boolean possibleRecipe;
        
        public NewRecipesFilter() {
            urls = new HashSet<>();
            possibleRecipe = false;
        }
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            stack++;
            if (possibleRecipe) {
                if (stack < 2) {
                    String styleClass = atts.getValue("class");
                    if ("div".equals(qName) && styleClass.contains("teaser__rating")) {
                        possibleRecipe = false;
                        urls.add(new UrlString(url));
                    }
                }
                if (stack < 0) {
                    possibleRecipe = false;
                }
            } else if ("a".equals(qName) && "teaser__link teaser__link--overlay".equals(atts.getValue("class"))) {
                possibleRecipe = true;
                url = atts.getValue("href");
                stack = 1;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            stack--;
        }

        public Set<UrlString> getUrls() {
            return urls;
        }
    
    }
    
    class KeywordFilter extends XMLFilterImpl {

        Set<String> keywords;
        
        public KeywordFilter() {
            keywords = new HashSet<>();
        } 
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if ("meta".equals(qName)) {
                if ("article:tag".equals(atts.getValue("property"))) {
                    String keyword = atts.getValue("content");
                    if (null != keyword) {
                        if (keyword.toUpperCase().contains("SALAT")) {
                            keywords.add("SALAT");
                        } 
                        keywords.add(keyword);
                    }
                }
            }
            super.startElement(uri, localName, qName, atts);
        }

        public Set<String> getKeywords() {
            return keywords;
        }
        
    }
}
