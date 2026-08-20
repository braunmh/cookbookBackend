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
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import static org.braun.cookbook.backend.crawler.CrawlerBase.LOG;
import org.braun.cookbook.backend.model.BackgroundJobType;
import org.braun.cookbook.backend.model.Recipe;
import org.braun.cookbook.backend.model.recipe.Category;
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
@Named
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class WdrHierUndHeuteCrawler extends CrawlerBase<UrlAuthor> {

    private static final List<String> koeche = List.of("Lars Middendorf", "Karin Steinhoff", "Matthias Ludwigs",
            "Björn Freitag", "Theresa Knipschild", "Julia Komp", "Julia Floß", "Ulla Scholz",
            "Matthias Ludwigs", "Alexander Wulf", "Olaf Baumeister", "Fabian Timmer", "Marcel Seeger",
            "Cornelia Baumgart", "Emily Gorden", "Frank Buchholz");

    @Override
    protected String getPathParent(Recipe recipe, UrlAuthor url) {
        Calendar now = Calendar.getInstance();
        return "ARD/wdr/hierUndHeute/" + now.get(Calendar.YEAR);
    }

    @Override
    protected Recipe getRecipe(UrlAuthor url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url.getUrl()))
                .GET()
                .build();
        try (
                HttpClient client = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .build(); InputStream inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();) {
                    Parser parser = new Parser();
                    parser.setFeature(Parser.namespacePrefixesFeature, false);
                    InputSource inputSource = new InputSource(inputStream);
                    Recipe recipe = new Recipe();
                    recipe.getSource().setUrl(url.getUrl());
                    ArticleFilter articleFilter = new ArticleFilter(recipe);
                    articleFilter.setParent(parser);
                    WdrEinfachKoestlichCrawler.WdrMetaFilter metaFilter = new WdrEinfachKoestlichCrawler.WdrMetaFilter(recipe);
                    VideoObjectLdJsonFilter videoObjectLdJsonFilter = new VideoObjectLdJsonFilter();
                    videoObjectLdJsonFilter.setParent(articleFilter);
                    metaFilter.setParent(videoObjectLdJsonFilter);
                    metaFilter.parse(inputSource);
                    boolean authorAdded = false;
                    if (videoObjectLdJsonFilter.getVideoObjectLdJson() != null) {
                        for (String keyword : videoObjectLdJsonFilter.getVideoObjectLdJson().getKeywords()) {
                            if (koeche.contains(keyword)) {
                                recipe.getSource().setValue(recipe.getSource().getValue() + " (" + keyword + ")");
                                authorAdded = true;
                            } else {
                                recipe.getCategories().add(new Category().name(keyword));
                            }
                        }
                    }
                    if (!authorAdded && url.getAuthor() != null) {
                        recipe.getSource().setValue(recipe.getSource().getValue() + " (" + url.getAuthor() + ")");
                    }
                    if (StringUtils.isBlank(recipe.getTitle())) {
                        LOG.info("Empty Recipe ", url);
                        return null;
                    }
                    return recipe;
                } catch (IOException | SAXException | InterruptedException e) {
                    LOG.error("Unable to read recipe form url " + url);
                } catch (Exception e) {
                    LOG.error("Unexpected Exception {} processing url {}", e.getMessage(), url.getUrl());
                }
                return null;
    }

    @Override
    protected List<UrlAuthor> getNewRecipes() {
        List<UrlAuthor> indexPages = List.of(
                new UrlAuthor("/verbraucher/rezepte/koeche/uebersicht-kochen-mit-martina-und-moritz-100.html").author("Martina Meuth und Bernd Neuner-Duttenhofer"),
                new UrlAuthor("/verbraucher/rezepte/koeche/uebersicht-bjoern-freitag-100.html").author("Björn Freitag"),
                new UrlAuthor("/verbraucher/rezepte/koeche/uebersicht-theresa-knipschild-100.html").author("Theresa Knipschild"),
                new UrlAuthor("/verbraucher/rezepte/koeche/uebersicht-julia-komp-100.html").author("Julia Komp"),
                new UrlAuthor("/verbraucher/rezepte/koeche/uebersicht-ullas-erfolgsrezepte-100.html").author("Ulla Scholz"),
                new UrlAuthor("/verbraucher/rezepte/koeche/uebersicht-lars-middendorf-100.html").author("Lars Middendorf"),
                new UrlAuthor("/verbraucher/rezepte/koeche/uebersicht-matthias-ludwigs-100.html").author("Matthias Ludwigs"),
                new UrlAuthor("/verbraucher/rezepte/koeche/uebersicht-alexander-wulf-100.html").author("Alexander Wulf"),
                new UrlAuthor("/verbraucher/rezepte/koeche/olaf-baumeister-uebersicht-100.html").author("Olaf Baumeister"),
                new UrlAuthor("/verbraucher/rezepte/koeche/uebersicht-marcel-seeger-100.html").author("Marcel Seeger"),
                new UrlAuthor("/verbraucher/rezepte/koeche/uebersicht-fabian-timmer-100.html").author("Fabian Timmer"),
                new UrlAuthor("/fernsehen/hier-und-heute/uebersicht-alle-rezepte-huh-100.html").author(null)
        );
        String prefix = "https://www1.wdr.de";
        Set<UrlAuthor> urls = new HashSet<>();
        for (UrlAuthor indexPage : indexPages) {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(prefix + indexPage.getUrl()))
                    .GET()
                    .build();
            OverviewFilter overviewFilter = new OverviewFilter(prefix, indexPage.getAuthor());
            try (HttpClient client = HttpClient.newBuilder()
                            .followRedirects(HttpClient.Redirect.NORMAL)
                            .version(HttpClient.Version.HTTP_2)
                            .build(); InputStream inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();) {
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
                    urls.addAll(overviewFilter.getUrls());
        }
        return new ArrayList(urls);
    }

    @Override
    public BackgroundJobType getTaskName() {
        return BackgroundJobType.WdrHierUndHeuteCrawler;
    }

    class OverviewFilter extends XMLFilterImpl {

        private enum Step {
            parse, link
        };

        private static List<String> linkClass = List.of("teaser", "hideTeasertext");
        private Step step;
        private final Set<UrlAuthor> urls;
        private final String prefix;
        private int stack;
        private final String author;

        public OverviewFilter(String prefix, String author) {
            this.prefix = prefix;
            urls = new HashSet<>();
            step = Step.parse;
            stack = 0;
            this.author = author;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            stack++;
            switch (step) {
                case parse -> {
                    if ("div".equals(qName) && containsAll(atts.getValue("class"), linkClass)) {
                        step = Step.link;
                        stack = 0;
                    }
                }
                case link -> {
                    if ("a".equals(qName)) {
                        if (recipeUrl(atts.getValue("href"))) {
                            urls.add(new UrlAuthor(prefix + atts.getValue("href")).author(author));
                            step = Step.parse;
                        }
                    }
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            stack--;
            if (stack < 0) {
                step = Step.parse;
            }
        }

        private boolean recipeUrl(String url) {
            return !(url == null || url.startsWith("javascript") || url.endsWith("index.html")
                    || url.contains("/fernsehen/") || url.endsWith("newsletterRezepte100.html")
                    || url.contains("https://www.ardmediathek.de"));
        }

        private boolean containsAll(String value, List<String> values) {
            if (value == null) {
                return false;
            }
            return Arrays.asList(value.split(" ")).containsAll(values);
        }

        public Set<UrlAuthor> getUrls() {
            return urls;
        }
    }

    class ArticleFilter extends XMLFilterImpl {

        private enum Step {
            parse, article, ingredient, preparation, ingredientORpreparation, epilog, mediaplayer
        }

        private final Set<Step> writeContent = Set.of(Step.ingredient, Step.preparation, Step.ingredientORpreparation, Step.epilog);

        private final List<String> ingredientORpreparationClass = List.of("mod", "modA", "modParagraph");
        private final List<String> headingClass = List.of("subtitle", "small");
        private final List<String> ingredientClass = List.of("checklist", "small");
        private final List<String> preparationClass = List.of("text", "small");
        private final List<String> epilogClass = List.of("einleitung", "small");
        private final List<String> mediplayerClass = List.of("mediaCon", "mediaTop", "videoRatio", "landscape");

        boolean isScript;

        private Step step;

        private final Recipe recipe;

        private final CharArrayWriter writer;

        private String heading;

        private int stack;
        private int stackMediaplayer;

        public ArticleFilter(Recipe recipe) {
            this.recipe = recipe;
            step = Step.parse;
            writer = new CharArrayWriter();
            stack = 0;
            stackMediaplayer = 0;
            heading = "";
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            stack++;
            stackMediaplayer++;
            isScript = "script".equals(qName);
            switch (step) {
                case parse -> {
                    if ("article".equals(qName)) {
                        step = Step.article;
                    }
                }
                case article -> {
                    if ("div".equals(qName) && containsAll(atts.getValue("class"), ingredientORpreparationClass)) {
                        step = Step.ingredientORpreparation;
                        stack = 0;
                    }
                }
                case ingredientORpreparation -> {
                    switch (qName) {
                        case "h2" -> {
                            if (containsAll(atts.getValue("class"), headingClass)) {
                                writer.reset();
                            }
                        }
                        case "div" -> {
                            String styleClass = atts.getValue("class");
                            if (containsAll(styleClass, ingredientClass)) {
                                if ("zubereitung:".equalsIgnoreCase(heading)) {
                                    step = Step.preparation;
                                }
                                recipe.addIngredients(new Ingredients().title(heading));
                                step = Step.ingredient;
                            } else if (containsAll(styleClass, mediplayerClass)) {
                                step = Step.mediaplayer;
                                stackMediaplayer = 0;
                            }
                        }
                        case "p" -> {
                            if (containsAll(atts.getValue("class"), preparationClass)) {
                                step = Step.preparation;
                                writer.reset();
                            } else if ("p".equals(qName) && containsAll(atts.getValue("class"), epilogClass)) {
                                step = Step.epilog;
                                writer.reset();
                            }
                        }
                    }
                }
                case ingredient -> {
                    if ("li".equals(qName)) {
                        writer.reset();
                    }
                }
                case preparation -> {
                    if ("p".equals(qName)) {
                        writer.reset();
                    }
                }
            }
            super.startElement(uri, localName, qName, atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            stack--;
            stackMediaplayer--;
            if (isScript) {
                isScript = !"script".equals(qName);
            }
            switch (step) {
                case epilog -> {
                    if ("p".equals(qName)) {
                        recipe.getDescription().add(new Paragraph().value(writer.toString()));
                        writer.reset();
                        step = Step.ingredientORpreparation;
                    }
                }
                case ingredientORpreparation -> {
                    switch (qName) {
                        case "h2" -> {
                            heading = writer.toString().trim();
                            if ("Das Rezept".equals(heading)) {
                                heading = "";
                            } else if (heading.startsWith("Zutaten")) {
                                heading = heading.substring(7).trim();
                            }
                            writer.reset();
                        }
                        case "div" -> {
                            if (stack <= 0) {
                                step = Step.article;
                            }
                        }
                    }
                }
                case ingredient -> {
                    switch (qName) {
                        case "li" -> {
                            recipe.getLastIngredients().add(Ingredient.parse(writer.toString().trim()));
                        }
                        case "ul", "ol" -> {
                            step = Step.ingredientORpreparation;
                            if (recipe.getLastIngredients() != null && !recipe.getLastIngredients().getTitle().isEmpty()) {
                                recipe.getDescription().add(new Heading().value(recipe.getLastIngredients().getTitle()));
                            }
                        }
                    }
                }
                case preparation -> {
                    if ("p".equals(qName)) {
                        recipe.getDescription().add(new Paragraph().value(writer.toString()));
                        step = Step.ingredientORpreparation;
                    } else if ("br".equals(qName)) {
                        recipe.getDescription().add(new Paragraph().value(writer.toString()));
                        writer.reset();
                    }
                }
                case mediaplayer -> {
                    if (stackMediaplayer <= 0) {
                        step = Step.ingredientORpreparation;
                    }
                }
            }
            super.endElement(uri, localName, qName);
        }

        private boolean containsAll(String value, List<String> values) {
            if (value == null) {
                return false;
            }
            return Arrays.asList(value.split(" ")).containsAll(values);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (writeContent.contains(step) && !isScript) {
                writer.write(ch, start, length);
            }
            super.characters(ch, start, length);
        }

    }
}
