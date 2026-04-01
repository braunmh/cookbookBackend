package org.braun.cookbook.backend.crawler;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.braun.cookbook.backend.crawler.Crawler.LOG;
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
public class ArdSwrCrawler extends Crawler {

    
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
            RecipeLd recipeLd = RecipeArd.parse(inputStream);
            return recipeLd.toRecipe();
        } catch (IOException | InterruptedException e) {
            LOG.error("execute failed with", e);
        }
        return null;
    }

    @Override
    protected String getPathParent() {
        Calendar now = Calendar.getInstance();
        return "ARD/swr/" + now.get(Calendar.YEAR);
    }

    @Override
    protected List<String> getNewRecipes() {
        String prefix = "https://www.swr.de";
        OverviewCommenFilter overviewCommonfilter = new OverviewCommenFilter(prefix);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(prefix + "/leben/rezepte"))
                .GET()
                .build();

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        
        try (InputStream inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();) {
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
        for (String url : overviewCommonfilter.getUrls()) {
            System.out.println(url);
        }
        Set<String> result = new HashSet<>(overviewCommonfilter.getUrls());
        
        System.out.println("Ard-Buffet");
        request = HttpRequest.newBuilder()
                .uri(URI.create(prefix + "/leben/rezepte/rezepte-archiv-102.html"))
                .GET()
                .build();
        
        OverviewBuffetFilter overviewBuffetFilter = new OverviewBuffetFilter(prefix);
        try (InputStream inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();) {
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
        for (String url : overviewBuffetFilter.getUrls()) {
            System.out.println(url);
        }
        result.addAll(overviewBuffetFilter.getUrls());
        List<String> res = new ArrayList<>(result);
    return res;
    }
    
    class OverviewCommenFilter extends XMLFilterImpl {
        enum ParseType  {
            other, carousel, header, href
        };
        
        private final Set<String> urls;
        
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
                        urls.add(url);
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

        public Set<String> getUrls() {
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
        
        private final Set<String> urls;
        
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
                        urls.add(url);
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

        public Set<String> getUrls() {
            return urls;
        }
        
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
        }
    }
}
