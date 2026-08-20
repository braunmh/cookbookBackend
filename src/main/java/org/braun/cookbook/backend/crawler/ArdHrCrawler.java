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
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
@Named("ardHrCrawler")
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ArdHrCrawler extends Crawler {

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
            
            CleanFilter cleanFilter = new CleanFilter(List.of("script", "noscript", "svg", "link"));
            cleanFilter.setParent(parser);
            
            ArticleFilter articleFilter = new ArticleFilter(recipe);
            articleFilter.setParent(cleanFilter);
            
            MetaFilter metaFilter = new MetaFilter(recipe);
            metaFilter.setParent(articleFilter);
            
            metaFilter.parse(inputSource);
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
        return "ARD/hr/" + now.get(Calendar.YEAR);
    }

    @Override
    protected List<UrlString> getNewRecipes() {
        String prefix = "https://www.hr-fernsehen.de";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(prefix + "/rezepte/index.html"))
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
        return BackgroundJobType.ArdHrCrawler;
    }
    
    class ArticleFilter extends XMLFilterImpl {
        enum Step {
            other, article, title, heading, ingredients, preparations, finished;
        }
        
        private boolean parseIngredients;
        
        private Step step;
        
        private final Set<Step> charcaters = Set.of(Step.heading, Step.ingredients, Step.preparations, Step.title);
        
        private final Recipe recipe;
        
        private final CharArrayWriter writer;
        
        public ArticleFilter(Recipe recipe) {
            this.recipe = recipe;
            step = Step.other;
            writer = new CharArrayWriter();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            switch (step) {
                case other -> {
                    if ("article".equals(localName)) {
                        step = Step.article;
                    }
                }
                case article -> {
                    if ("h2".equals(localName)) {
                        step = Step.heading;
                    } else if ("header".equals(localName)) {
                        step = Step.title;
                    }
                }
                case title -> {
                    if ("h2".equals(localName)) {
                        writer.reset();
                    }
                }
                case ingredients -> {
                    switch (localName) {
                        case "ul" -> {
                            if (recipe.getIngredients().isEmpty()) {
                                recipe.addIngredients(new Ingredients());
                            }
                        }
                        case "li", "p", "h2" -> {
                            writer.reset();
                        }
                    }
                }
                case preparations -> {
                    if ("li".equals(localName) || "p".equals(localName)) {
                        writer.reset();
                    }
                }
            }
            super.startElement(uri, localName, qName, atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (step) {
                case heading -> {
                    if ("h2".equals(localName)) {
                        String heading = writer.toString().toUpperCase();
                        if (heading.startsWith("ZUTATEN")) {
                            parseIngredients = true;
                        } else if (heading.startsWith("ZUBEREITUNG")) {
                            parseIngredients = false;
                        }
                        step = (parseIngredients) ? Step.ingredients : Step.preparations;
                        writer.reset();
                    }
                }
                case ingredients -> {
                    switch (localName) {
                        case "h2" -> {
                            String heading = writer.toString().toUpperCase();
                            if (heading.startsWith("ZUBEREITUNG")) {
                                parseIngredients = false;
                                step = Step.preparations;
                            }
                        }
                        case "li" -> {
                            recipe.getLastIngredients().add(Ingredient.parse(writer.toString()));
                        }
                        case "p" -> {
                            recipe.addIngredients(new Ingredients().title(writer.toString().trim()));
                        }
                        case "article" -> {
                            step = Step.finished;
                        }
                    }
                }
                case preparations -> {
                    switch (localName) {
                        case "li" -> {
                            recipe.getDescription().add(new Paragraph().value(writer.toString()));
                        }
                        case "p" -> {
                            String heading = writer.toString().trim();
                            if (heading.startsWith("Das Rezept im Video zum Nachschauen")
                                || heading.startsWith("An dieser Stelle befindet sich")) {
                                step = Step.finished;
                                return;
                            }
                            recipe.getDescription().add(new Heading().value(heading));
                            writer.reset();
                        }
                        case "article" -> {
                            step = Step.finished;
                        }
                    }
                }
                case title -> {
                    if ("h2".equals(localName)) {
                        if (StringUtils.isBlank(recipe.getTitle())) {
                            String title = writer.toString().trim();
                            if (title.startsWith("Rezept")) {
                                title = title.substring(7);
                            }
                            recipe.setTitle(title);
                        }
                        writer.reset();
                        step = Step.article;
                    }
                }
            }
            super.endElement(uri, localName, qName);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (charcaters.contains(step)) {
                writer.write(ch, start, length);
            }
        }
        
    }
    
    public static class MetaFilter extends XMLFilterImpl {
        private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmZ");
        enum Step {
            meta, finish;
        }
        
        private Step step;
        
        private final Recipe recipe;
        
        public MetaFilter(Recipe recipe) {
            this.recipe = recipe;
            step = Step.meta;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if (step == Step.meta) {
                if ("meta".equals(localName)) {
                    switch (atts.getValue("property")) {
                        case null -> {}
                        case "og:image" -> {
                            recipe.setImageUrl(atts.getValue("content"));
                        }
                        case "og:image:height" -> {
                            recipe.setHeight(parseInt(atts.getValue("content")));
                        }
                        case "og:image:width" -> {
                            recipe.setWidth(parseInt(atts.getValue("content")));
                        }
                        case "article:published_time" -> {
                            try {
                                String value = atts.getValue("content");
                                if (value != null) {
                                    OffsetDateTime dateTime = OffsetDateTime.parse(value, formatter);
                                    recipe.setPublished(dateTime.toEpochSecond());
                                }
                            } catch (DateTimeParseException e) {
                                LOG.error("Unparseable OffsetDateTime {}. Url: {}", e.getMessage(), recipe.getSource().getUrl());
                            }
                        }
                        default -> {}
                    }
                } else if ("body".equals(localName)) {
                    step = Step.finish;
                }
            }
            super.startElement(uri, localName, qName, atts);
        }

        private int parseInt(String value) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
    }
    
    class OverviewFilter extends XMLFilterImpl {
        
        enum Step {
            other, section;
        }
        
        private Step step;
        
        private final Set<UrlString> urls;
        
        private final String prefix;
        
        public OverviewFilter(String prefix) {
            this.prefix = prefix;
            urls = new HashSet<>();
            step = Step.other;
        }

        public Set<UrlString> getUrls() {
            return urls;
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
                    if ("a".equals(localName)) {
                        String url = atts.getValue("href");
                        if (url != null && !url.endsWith("/index.html")) {
                            if (url.contains("://")) {
                                urls.add(new UrlString(url));
                            } else {
                                urls.add(new UrlString(prefix + url));
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (step == Step.section && "section".equals(localName)) {
                step = Step.other;
            }
        }
        
    }
}
