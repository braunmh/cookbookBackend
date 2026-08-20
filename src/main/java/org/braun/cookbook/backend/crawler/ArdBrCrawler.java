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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import static org.braun.cookbook.backend.crawler.Crawler.LOG;
import org.braun.cookbook.backend.model.BackgroundJobType;
import org.braun.cookbook.backend.model.Recipe;
import org.braun.cookbook.backend.model.recipe.Heading;
import org.braun.cookbook.backend.model.recipe.Ingredient;
import org.braun.cookbook.backend.model.recipe.Ingredients;
import org.braun.cookbook.backend.model.recipe.Paragraph;
import org.braun.cookbook.backend.model.recipe.Yield;
import org.braun.cookbook.common.EndOfProcessing;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Crawler für Rezepte des Bayrischen Rundfunks aus der Rubrik "Wir in Bayern"
 *
 * @author mbraun
 */
@Named("ardBrCrawler")
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ArdBrCrawler extends Crawler {

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
            Recipe recipe = new Recipe();
            recipe.getSource().setUrl(url.getUrl());
            recipe.getSource().setValue("Wir in Bayern");
            
            CleanFilter cleanFilter = new CleanFilter(List.of("script", "noscript", "svg", "link"));
            cleanFilter.setParent(parser);
            
            ArticleFilter articleFilter = new ArticleFilter(recipe);
            articleFilter.setParent(cleanFilter);
            
            
            articleFilter.parse(inputSource);
            if (StringUtils.isBlank(recipe.getTitle())) {
                LOG.info("Empty Recipe ", url);
                return null;
            }
            return recipe;
        } catch (IOException | SAXException | InterruptedException e) {
            LOG.error("Unable to read recipe form url " + url);
        }
        return null;
    }

    @Override
    protected String getPathParent() {
        Calendar now = Calendar.getInstance();
        return "ARD/br/" + now.get(Calendar.YEAR);
    }

    @Override
    protected List<UrlString> getNewRecipes() {
        String prefix = "https://www.br.de";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(prefix + "/br-fernsehen/sendungen/wir-in-bayern/rezepte/index.html"))
                .GET()
                .build();
        
        OverviewFilter overviewFilter = new OverviewFilter(prefix);
        try (HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
            InputStream inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();) {
            if (inputStream != null) {
                Parser reader = new Parser();
                reader.setFeature(Parser.namespacePrefixesFeature, false);
                InputSource inputSource = new InputSource(inputStream);
                overviewFilter.setParent(reader);
                overviewFilter.parse(inputSource);
            }
        } catch (EndOfProcessing e) {
            // ignore
        } catch (SAXException | IOException | InterruptedException e) {
            LOG.error("execute failed with", e);
        }
        return new ArrayList<>(overviewFilter.getUrls());
    }

    @Override
    public BackgroundJobType getTaskName() {
        return BackgroundJobType.ArdBrCrawler;
    }
 
    private class ArticleFilter extends XMLFilterImpl {
        enum Step {
            other, finish, article, content, prepartion, ingredient, title, yield, intro, image
        }
        private final Recipe recipe;
        private final CharArrayWriter writer;
        private Step step;
        private String stepTitle;
        
        public ArticleFilter(Recipe recipe) {
            this.recipe = recipe;
            writer = new CharArrayWriter();
            step = Step.other;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            switch (step) {
                case other -> {
                    if ("article".equals(localName)) {
                        step = Step.title;
                    } else if ("meta".equals(localName) && "twitter:image:src".equals(atts.getValue("property"))) {
                        recipe.setImageUrl(atts.getValue("content"));
                    }
                }
                case intro -> {
                    if ("p".equals(localName) && "detail_lead".equals(atts.getValue("class"))) {
                        writer.reset();
                    }
                }
                case content -> {
                    switch(localName) {
                        case "div" -> writer.reset();
                        case "h2" -> stepTitle = "";
                        case "ul" -> {
                            step = Step.ingredient;
                            recipe.addIngredients(new Ingredients().title(stepTitle));
                        }
                        case "p" -> step = Step.prepartion;
                    }
                }
                case ingredient -> {
                    if ("li".equals(localName)) {
                        writer.reset();
                    }
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (step) {
                case title -> {
                    switch (localName) {
                        case "em" -> writer.reset();
                        case "h1" -> {
                            recipe.setTitle(writer.toString().trim());
                            step = Step.intro;
                        }
                    }
                }
                case intro -> {
                    if ("p".equals(localName)) {
                        step = Step.content;
                        String text = writer.toString().trim();
                        if (!text.isEmpty()) {
                            recipe.getDescription().add(new Paragraph().value(text));
                            writer.reset();
                        }
                    } else if ("header".equals(localName)) {
                        step = Step.content;
                    }
                }
                case content -> {
                    switch(localName) {
//                        case "div" -> writer.reset();
                        case "h2" -> {
                            stepTitle = writer.toString().trim();
                            writer.reset();
                        }
                        case "article" -> {
                            step = Step.finish;
                        }
                    }
                }
                case ingredient -> {
                    switch (localName) {
                        case "ul" -> step = Step.content;
                        case "li" -> {
                            recipe.getLastIngredients().add(Ingredient.parse(writer.toString()));
                        }
                        case "article" -> step = Step.finish;
                    }
                }
                case prepartion -> {
                    switch (localName) {
                        case "p" -> {
                            if (StringUtils.isNotBlank(stepTitle)) {
                                recipe.getDescription().add(new Heading().value(stepTitle));
                                stepTitle = null;
                            }
                            String text = writer.toString().trim().replaceAll("\u00a0|\\n|\\t", " ");
                            if (text.startsWith("Stand: ")) {
                                String[] values = text.split(" ");
                                if (values.length > 2) {
                                    recipe.getSource().setValue("Wir in Bayern " + values[1]);
                                }
                            } else if (text.startsWith("Rezept für ")) {
                                String[] values = text.split(" ");
                                if (values.length > 3) {
                                    recipe.setYield(Yield.parse(values[2], values[3]));
                                }
                            } else {
                                recipe.getDescription().add(new Paragraph().value(text));
                            }
                            writer.reset();
                        }
                        case "strong" -> writer.reset();
                        case "div" -> step = Step.content;
                        case "article" -> step = Step.finish;
                        case "span" -> step = Step.content;
                    }
                }
            }
        }
        
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (!(step == Step.finish || step == Step.other)) {
                writer.write(ch, start, length);
            }
        }
        
    }
    
    private class OverviewFilter extends XMLFilterImpl {
        
        enum Step {
            other, section, header, recipes, finish;
        }
        
        private Step step;
        private final Set<UrlString> urls;
        private final String prefix;
        private final CharArrayWriter writer;
        
        public OverviewFilter(String prefix) {
            this.prefix = prefix;
            urls = new HashSet<>();
            step = Step.other;
            writer = new CharArrayWriter();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            switch (step) {
                case other -> {
                    if ("section".equals(localName)) {
                        step = Step.section;
                    }
                }
                case section -> {
                    if ("header".equals(localName)) {
                        step = Step.header;
                    }
                }
                case header -> {
                    if ("h3".equals(localName)) {
                        writer.reset();
                    }
                }
                case recipes -> {
                    if ("a".equals(localName)) {
                        urls.add(new UrlString(prefix + atts.getValue("href")));
                    }
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch(step) {
                case header -> {
                    if ("h3".equals(localName)) {
                        String header = writer.toString().trim();
                        step = ("Vorschau & letzte Rezepte".equals(header)) ? Step.recipes : Step.other;
                    }
                }
                case recipes -> {
                    if ("section".equals(localName)) {
                        step = Step.finish;
                    }
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (step == Step.header) {
                writer.write(ch, start, length);
            }
        }

        public Set<UrlString> getUrls() {
            return urls;
        }
        
    }
}
