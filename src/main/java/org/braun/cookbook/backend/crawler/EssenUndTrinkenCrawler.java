package org.braun.cookbook.backend.crawler;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Named;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import static org.braun.cookbook.backend.crawler.Crawler.LOG;
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
@Named
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class EssenUndTrinkenCrawler extends Crawler {

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

            KeywordFilter keywordFilter = new KeywordFilter();
            keywordFilter.setParent(jsonFilter);
            keywordFilter.parse(inputSource);

            RecipeLd recipe = getRecipeFromJson(jsonFilter.getJson());
            if (recipe == null) {
                return null;
            }
            recipe.setRecipeCategory(new Text().addAll(keywordFilter.getCategories()));
            return recipe.toRecipe();
        } catch (SAXException | IOException | InterruptedException e) {
            LOG.error("execute failed with", e);
        }
        return null;
    }

    @Override
    protected String getPathParent() {
        Calendar now = Calendar.getInstance();
        return "essenTrinken/" + now.get(Calendar.YEAR);
    }

    @Override
    protected List<String> getNewRecipes() {
        String prefix = "https://www.essen-und-trinken.de";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(prefix))
                .GET()
                .build();
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        OverviewFilter overviewFilter = new OverviewFilter();
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
        return BackgroundJobType.EssenUndTrinkenCrawler;
    }
    
    class KeywordFilter extends XMLFilterImpl {
        private enum Step{other, categories, category, finished};
        
        private Step step;
        private final List<String> categories;
        private final CharArrayWriter writer;
        
        private int stack;
        public KeywordFilter() {
            categories = new ArrayList<>();
            step = Step.other;
            writer = new CharArrayWriter();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            stack++;
            switch (step) {
                case other -> {
                    if ("div".equals(localName)) {
                        String classAtt = atts.getValue("class");
                        if (classAtt != null && classAtt.contains("recipe-meta__item--categories")) {
                            stack = 0;
                            step = Step.categories;
                        }
                    }
                }
                case categories -> {
                    if ("a".equals(localName)) {
                        step = Step.category;
                        writer.reset();
                    }
                }
            }
            super.startElement(uri, localName, qName, atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            stack--;
            switch (step) {
                case category -> {
                    if ("a".equals(localName)) {
                        step = Step.categories;
                        categories.add(writer.toString().trim());
                    }
                }
                case finished -> { }
                default -> {
                    if (stack < 0) {
                        step = Step.finished;
                    }
                }
            }
            super.endElement(uri, localName, qName);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (step == Step.category) {
                writer.write(ch, start, length);
            }
            super.characters(ch, start, length);
        }

        public List<String> getCategories() {
            return categories;
        }
        
    }
    
    class OverviewFilter extends XMLFilterImpl {
        enum Step {
            other, overview, article, finished
        }
        
        private int stack;
        
        private Step step;
        
        private List<String> urls;
        
        public OverviewFilter() {
            step = Step.other;
            urls = new ArrayList<>();
            stack = 0;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            stack++;
            switch (step) {
                case other -> {
                    if ("div".equals(localName) && "Unsere_20neuesten_20Rezeptideen_20und_20Tipps".equals(atts.getValue("id"))) {
                        step = Step.overview;
                        stack = 0;
                    }
                }
                case overview -> {
                    if ("article".equals(localName)) {
                        step = Step.article;
                    }
                }
                case article -> {
                    if ("a".equals(localName)) {
                        String url = atts.getValue("href");
                        if (url.contains("/rezepte/")) {
                            urls.add(url);
                        }
                        step = Step.overview;
                    }
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            stack--;
            if (stack < 0) {
                throw new EndOfProcessing();
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
        }

        public List<String> getUrls() {
            return urls;
        }
        
    }
}
