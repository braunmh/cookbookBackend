package org.braun.cookbook.backend.crawler;

import org.braun.cookbook.common.EndOfProcessing;
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
import static org.braun.cookbook.backend.crawler.Crawler.LOG;
import org.braun.cookbook.backend.model.BackgroundJobType;
import org.braun.cookbook.backend.model.Recipe;
import org.braun.cookbook.backend.model.RecipeLd;
import org.braun.cookbook.backend.model.recipeLd.RecipeArd;
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
public class ArdSwrCrawler extends Crawler {
    
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
            RecipeLd recipeLd = RecipeArd.parse(inputStream);
            if (recipeLd != null) {
                Recipe recipe = recipeLd.toRecipe();
                if (recipe.getTitle() != null && recipe.getTitle().startsWith("Rezept:")) {
                    recipe.setTitle(recipe.getTitle().substring(8));
                }
                return recipe;
            }
            return null;
            
        } catch (IOException | InterruptedException e) {
            LOG.error("execute failed with", e);
        }
        return null;
    }

    @Override
    public BackgroundJobType getTaskName() {
        return BackgroundJobType.ArdSwrCrawler;
    }

    @Override
    protected String getPathParent() {
        Calendar now = Calendar.getInstance();
        return "ARD/swr/" + now.get(Calendar.YEAR);
    }

    @Override
    protected List<UrlString> getNewRecipes() {
        String prefix = "https://www.swr.de";
        OverviewCommenFilter overviewCommonfilter = new OverviewCommenFilter(prefix);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(prefix + "/leben/rezepte"))
                .GET()
                .build();

        try (HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
                InputStream inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();) {
            if (inputStream != null) {
                Parser reader = new Parser();
                reader.setFeature(Parser.namespacePrefixesFeature, false);
                InputSource inputSource = new InputSource(inputStream);
                overviewCommonfilter.setParent(reader);
                overviewCommonfilter.parse(inputSource);
            }
        } catch (EndOfProcessing e) {
            // ignore
        } catch (SAXException | IOException | InterruptedException e) {
            LOG.error("execute failed with", e);
        }
        Set<UrlString> result = new HashSet<>(overviewCommonfilter.getUrls());
        
        request = HttpRequest.newBuilder()
                .uri(URI.create(prefix + "/leben/rezepte/rezepte-archiv-102.html"))
                .GET()
                .build();
        
        OverviewBuffetFilter overviewBuffetFilter = new OverviewBuffetFilter(prefix);
        try (HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
                InputStream inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();) {
            if (inputStream != null) {
                Parser reader = new Parser();
                reader.setFeature(Parser.namespacePrefixesFeature, false);
                InputSource inputSource = new InputSource(inputStream);
                overviewBuffetFilter.setParent(reader);
                overviewBuffetFilter.parse(inputSource);
            }
        } catch (EndOfProcessing e) {
            // ignore
        } catch (SAXException | IOException | InterruptedException e) {
            LOG.error("execute failed with", e);
        }
        result.addAll(overviewBuffetFilter.getUrls());
        List<UrlString> res = new ArrayList<>(result);
    return res;
    }
    
    class OverviewCommenFilter extends XMLFilterImpl {
        enum ParseType  {
            other, carousel, header, href
        };
        
        private final Set<UrlString> urls;
        
        private final String prefix;
        
        private OverviewCommenFilter.ParseType step;
        
        protected int stack;
        protected int ptStack;
        protected CharArrayWriter characters;
        
        public OverviewCommenFilter(String prefix) {
            this.prefix = prefix;
            urls = new HashSet<>();
            stack = 0;
            characters = new CharArrayWriter();
            step = ParseType.other;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            stack++;
            switch (step) {
                case other -> {
                    if ("div".equals(localName) && "carousel".equals(atts.getValue("class"))) {
                        step = ParseType.carousel;
                        initStack();
                    }
                }
                case carousel -> {
                    if ("header".equals(localName)) {
                        step = ParseType.header;
                    }
                }
                case header -> {
                    if ("a".equals(localName)) {
                        step = ParseType.carousel;
                        String url = prefix + atts.getValue("href");
                        urls.add(new UrlString(url));
                    }
                }
                        
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            stack--;
            if (step == ParseType.carousel && "div".equals(localName)) {
                if (stack <= ptStack) {
                    if (urls.isEmpty()) {
                        step = ParseType.other;
                    } else {
                        throw new EndOfProcessing();
                    }
                }
            }
        }
        
        protected void initStack() {
            ptStack = stack;
            characters.reset();
        }

        public Set<UrlString> getUrls() {
            return urls;
        }
        
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
        }
    }

    class OverviewBuffetFilter extends XMLFilterImpl {
        enum ParseType  {
            other, header, href
        };
        
        private final Set<UrlString> urls;
        
        private final String prefix;
        
        private ParseType step;
        
        protected int stack;
        protected int ptStack;
        protected CharArrayWriter characters;
        
        public OverviewBuffetFilter(String prefix) {
            this.prefix = prefix;
            urls = new HashSet<>();
            stack = 0;
            characters = new CharArrayWriter();
            step = ParseType.other;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            stack++;
            switch (step) {
                case other -> {
                    if ("div".equals(localName) && atts.getValue("id") != null && atts.getValue("id").startsWith("paginatedBlock")) {
                        step = ParseType.header;
                        initStack();
                    }
                }
                case header -> {
                    if ("header".equals(localName)) {
                        step = ParseType.href;
                    }
                }
                case href -> {
                    if ("a".equals(localName)) {
                        String url = prefix + atts.getValue("href");
                        urls.add(new UrlString(url));
                        step = ParseType.header;
                    }
                }       
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            stack--;
            if (step == ParseType.header && "div".equals(localName)) {
                if (stack < ptStack) {
                    if (urls.isEmpty()) {
                        step = ParseType.other;
                    } else {
                        throw new EndOfProcessing();
                    }
                }
            }
        }
        
        protected void initStack() {
            ptStack = stack;
            characters.reset();
        }

        public Set<UrlString> getUrls() {
            return urls;
        }
        
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
        }
    }
}
