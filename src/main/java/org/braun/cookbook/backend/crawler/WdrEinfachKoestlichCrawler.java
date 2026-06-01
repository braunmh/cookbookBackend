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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import static org.braun.cookbook.backend.crawler.Crawler.LOG;
import org.braun.cookbook.backend.model.BackgroundJobType;
import org.braun.cookbook.backend.model.Recipe;
import org.braun.cookbook.backend.model.recipe.Heading;
import org.braun.cookbook.backend.model.recipe.ImageSorter;
import org.braun.cookbook.backend.model.recipe.Ingredient;
import org.braun.cookbook.backend.model.recipe.Ingredients;
import org.braun.cookbook.backend.model.recipe.Paragraph;
import org.braun.cookbook.backend.model.recipe.Yield;
import org.braun.cookbook.backend.model.recipeLd.ImageObject;
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
public class WdrEinfachKoestlichCrawler extends Crawler {

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
            Recipe recipe = new Recipe();
            recipe.getSource().setUrl(url);
            ArticleFilter articleFilter = new ArticleFilter(recipe);
            articleFilter.setParent(parser);
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
        return "ARD/wdr/einfachKoestlich/" + now.get(Calendar.YEAR);
    }

    @Override
    protected List<String> getNewRecipes() {
        String prefix = "https://www1.wdr.de";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(prefix + "/verbraucher/rezepte/alle-rezepte/rezepte-uebersicht-einfach-und-koestlich-100.html"))
                .GET()
                .build();
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .version(HttpClient.Version.HTTP_2)
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
        return BackgroundJobType.WdrEinfachKoestlichCrawler;
    }

    class OverviewFilter extends XMLFilterImpl {

        enum Step {
            other, main, finished;
        }
        private String prefix;
        private Step step;

        Set<String> urls;
        int stack;

        public OverviewFilter(String prefix) {
            urls = new HashSet<>();
            stack = 0;
            step = Step.other;
            this.prefix = prefix;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            stack++;
            switch (step) {
                case other -> {
                    if ("div".equals(localName) && "section sectionA".equals(atts.getValue("class"))) {
                        step = Step.main;
                        stack = 0;
                    }
                }
                case main -> {
                    if ("a".equals(localName)) {
                        String url = atts.getValue("href");
                        if (url != null && url.startsWith("/verbraucher/rezepte/") && !url.endsWith("index.html")) {
                            urls.add(prefix + url);
                        }
                    }
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            stack--;
            if (stack == 0) {
                step = Step.other;
            }
        }

        public List<String> getUrls() {
            return new ArrayList<>(urls);
        }
    }

    class MetaFilter extends XMLFilterImpl {

        private final SimpleDateFormat isoDateTime = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        private final SimpleDateFormat isoDate = new SimpleDateFormat("dd.MM.YYYY");

        private final List<String> imageUrls;
        private final List<Integer> imageWidth;
        private final List<Integer> imageHeight;
        private final Recipe recipe;
        private String author;

        public MetaFilter(Recipe recipe) {
            this.recipe = recipe;
            imageUrls = new ArrayList<>();
            imageWidth = new ArrayList<>();
            imageHeight = new ArrayList<>();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if ("meta".equals(localName)) {
                String property = atts.getValue("property");
                String content = atts.getValue("content");
                if (content != null) {
                    if (property != null) {
                        switch (property) {
                            case "og:image" -> {
                                imageUrls.add(content);
                            }
                            case "og:image:width" -> {
                                imageWidth.add(toInt(content));
                            }
                            case "og:image:height" -> {
                                imageHeight.add(toInt(content));
                            }
                            case "article:published_time" -> {
                                Date d = toDate(content);
                                if (d != null) {
                                    recipe.setPublished(d.getTime());
                                }
                            }
                            case "article:modified_time" -> {
                                Date d = toDate(content);
                                if (d != null) {
                                    recipe.setModified(d.getTime());
                                }
                            }
                        }
                    } else if ("Author".equals(atts.getValue("name"))) {
                        author = content;
                    }
                }
            } else {
                super.startElement(uri, localName, qName, atts);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (!"meta".contentEquals(localName)) {
                super.endElement(uri, localName, qName);
            }
        }

        @Override
        public void endDocument() throws SAXException {
            int i = 0;
            List<ImageObject> images = new ArrayList<>();
            for (String imageUrl : imageUrls) {
                if (i < imageWidth.size() && i < imageHeight.size()) {
                    images.add(new ImageObject().name("").url(imageUrl).width(imageWidth.get(i)).height(imageHeight.get(i)));
                }
                i++;
            }
            Collections.sort(images, new ImageSorter().reversed());
            recipe.imageUrl(images.get(0).getUrl()).height(images.get(0).getHeight()).width(images.get(0).getWidth());
            StringBuilder source = new StringBuilder("WDR " + "Einfachköstlich");
            if (recipe.getPublished() != null) {
                source.append(" ").append(isoDate.format(new Date(recipe.getPublished())));
            }
            if (author != null) {
                source.append(" (").append(author).append(")");
            }
            recipe.getSource().setValue(source.toString());
            super.endDocument();
        }

        private int toInt(String value) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        private Date toDate(String value) {
            try {
                return isoDateTime.parse(value);
            } catch (ParseException e) {
                LOG.error("Can not parse date " + value);
                return null;
            }
        }
    }

    class ArticleFilter extends XMLFilterImpl {

        enum Step {
            parse, article, titleScan, title, introduction, content, sectionStart,
            prepare, plate, ingredient, prepareBreak, ingredientBreak, plateBreak, ignore;
        }
        private Step step;

        private final Recipe recipe;

        private final CharArrayWriter writer;

        private int stack;

        public ArticleFilter(Recipe recipe) {
            this.recipe = recipe;
            step = Step.parse;
            writer = new CharArrayWriter();
            stack = 0;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            stack++;
            if ("h1".equals(localName)) {
                if (atts.getValue("class").contains("articleHeader")) {
                    step = Step.titleScan;
                }
            }
            switch (step) {
                case parse -> {
                    if ("article".equals(localName)) {
                        step = Step.article;
                    }
                }
                case article -> {
                    if ("h1".equals(localName)) {
                        if (atts.getValue("class").contains("articleHeader")) {
                            step = Step.titleScan;
                        }
                    } else if ("p".equals(localName) && "einleitung small".equals(atts.getValue("class"))) {
                        step = Step.introduction;
                        writer.reset();
                    } else if ("div".equals(localName) && "mod modA modParagraph".equals(atts.getValue("class"))) {
                        writer.reset();
                        step = Step.content;
                        stack = 0;
                    }
                }
                case titleScan -> {
                    if ("span".equals(localName)) {
                        String classAtt = atts.getValue("class");
                        if (classAtt != null && classAtt.contains("heading")) {
                            writer.reset();
                            step = Step.title;
                        }
                    }
                }
                case content -> {
                    if ("p".equals(localName) && "text small".equals(atts.getValue("class"))) {
                        step = Step.sectionStart;
                        writer.reset();
                    }
                }
                case prepare, plate, ingredient -> {
                    if ("li".equals(localName)) {
                        writer.reset();
                    }
                }
                case ingredientBreak -> {
                    if ("ul".equals(localName)) {
                        step = Step.ingredient;
                    }
                }
                case prepareBreak -> {
                    if ("ul".equals(localName)) {
                        step = Step.prepare;
                    }
                }
                case plateBreak -> {
                    if ("ul".equals(localName)) {
                        step = Step.plate;
                    }
                }
            }
            super.startElement(uri, localName, qName, atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            stack--;
            if (step != Step.parse && "article".equals(localName)) {
                step = Step.ignore;
            }
            switch (step) {
                case title -> {
                    if ("span".equals(localName)) {
                        recipe.setTitle(writer.toString().trim());
                        writer.reset();
                        step = Step.article;
                    }
                }
                case introduction -> {
                    if ("p".equals(localName)) {
                        step = Step.article;
                        recipe.getDescription().add(new Paragraph().value(writer.toString().trim()));
                    }
                }
                case content -> {
                    if (stack == 0) {
                        step = Step.article;
                    }
                }
                case sectionStart -> {
                    if ("p".equals(localName)) {
                        String content = writer.toString().trim();
                        if (content.contains("Zutaten für ")) {
                            step = Step.content;
                            String[] values = content.split(" ");
                            if (values.length > 3) {
                                recipe.setYield(Yield.parse(values[values.length - 2], values[values.length - 1]));
                            }
                        } else {
                            switch (content) {
                                case "Zubereitung", "Zubereitung:" -> {
                                    step = Step.prepare;
                                    if (recipe.getLastIngredients() != null) {
                                        recipe.getDescription().add(new Heading().value(recipe.getLastIngredients().getTitle()));
                                    }
                                }
                                case "Anrichten", "Anrichten:" -> {
                                    step = Step.plate;
                                    recipe.getDescription().add(new Heading().value("Anrichten"));
                                }
                                case "Guten Apetit!" -> {
                                    step = Step.article;
                                }
                                default -> {
                                    if (content.startsWith("Für ") || content.contains("Zutaten")) {
                                        step = Step.ingredient;
                                        recipe.getIngredients().add(new Ingredients().title(content));
                                    }
                                }
                            }
                        }
                    } else if ("br".equals(localName)) {
                        String content = writer.toString().trim();
                        writer.reset();
                        if (content.contains("Zutaten")) {
                            step = Step.ingredientBreak;
                            recipe.addIngredients(new Ingredients().title(("Zutaten".equals(content)) ? "" : content));
                            String[] values = content.split(" ");
                            if (values.length > 3) {
                                recipe.setYield(Yield.parse(values[values.length - 2], values[values.length - 1]));
                            }
                        } else if (content.startsWith("Zubereitung")) {
                            step = Step.prepareBreak;
                        } else if (content.startsWith("Anrichten")) {
                            step = Step.plateBreak;
                            recipe.getDescription().add(new Heading().value("Anrichten"));
                        }
                    }
                }
                case ingredient -> {
                    if ("ul".equals(localName)) {
                        step = Step.content;
                    } else if ("li".equals(localName)) {
                        Ingredient ing = Ingredient.parse(writer.toString());
                        if (ing != null) {
                            recipe.getLastIngredients().add(ing);
                        }
                    }
                }
                case ingredientBreak -> {
                    if ("br".equals(localName) || "p".equals(localName)) {
                        Ingredient ing = Ingredient.parse(writer.toString());
                        writer.reset();
                        if (ing != null) {
                            recipe.getLastIngredients().add(ing);
                        }
                        if ("p".equals(localName)) {
                            step = Step.sectionStart;
                        }
                    }
                }
                case prepare, plate -> {
                    if ("ul".equals(localName)) {
                        step = Step.content;
                    } else if ("li".equals(localName)) {
                        recipe.getDescription().add(new Paragraph().value(writer.toString().trim()));
                    }
                }
                case prepareBreak, plateBreak -> {
                    if ("br".equals(localName) || "p".equals(localName)) {
                        recipe.getDescription().add(new Paragraph().value(writer.toString().trim()));
                        writer.reset();
                        if ("p".equals(localName)) {
                            step = Step.sectionStart;
                        }
                    }
                }
            }
            super.endElement(uri, localName, qName);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (step != Step.parse) {
                writer.write(ch, start, length);
            }
        }

    }
}
