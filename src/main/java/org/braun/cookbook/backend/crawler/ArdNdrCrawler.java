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
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.braun.cookbook.backend.model.BackgroundJobType;
import org.braun.cookbook.backend.model.Recipe;
import org.braun.cookbook.backend.model.RecipeLd;
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
public class ArdNdrCrawler extends Crawler {
    
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
            RecipeLd recipeLd = RecipeLd.parseNdr(inputStream);
            return (recipeLd == null) ? null : recipeLd.toRecipe();
        } catch (SAXException | IOException | InterruptedException e) {
            LOG.error("execute failed with", e);
        }
        return null;
    }
    
    
    @Override
    protected String getPathParent() {
        Calendar now = Calendar.getInstance();
        return "ARD/ndr/" + now.get(Calendar.YEAR);
    }
    
    @Override
    protected List<String> getNewRecipes() {
        String prefix = "https://www.ndr.de";
        OverviewFilter filter = new OverviewFilter(prefix);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(prefix + getSite()))
                .GET()
                .build();
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        
        try (InputStream inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();) {
            Parser reader = new Parser();
            reader.setFeature(Parser.namespacePrefixesFeature, false);
            InputSource inputSource = new InputSource(inputStream);
            filter.setParent(reader);
            filter.parse(inputSource);
        } catch (EndOfProcessing e) {
            // Just ignore
        } catch (SAXException | IOException | InterruptedException e) {
            LOG.error("execute failed with", e);
        }
        for (String url : filter.getUrls()) {
            System.out.println(url);
        }
        return filter.getUrls();
    }

    @Override
    public BackgroundJobType getTaskName() {
        return BackgroundJobType.ArdNdrCrawler;
    }

    protected String getSite() {
        return "/ratgeber/kochen";
    }
    
    class OverviewFilter  extends XMLFilterImpl {

        enum ParseType  {
            other, overviewSection, divTeaser, href
        };
        
        List<String> sectionOverviewClasses = List.of("teasergroup", "mosaik", "group-s-100", "group-m-33");
        private ParseType step;
        private final List<String> urls;
        protected int stack;
        protected int ptStack;
        protected CharArrayWriter characters;
        private final String prefix;

        public OverviewFilter(String prefix) {
            urls = new ArrayList<>();
            stack = 0;
            characters = new CharArrayWriter();
            step = ParseType.other;
            this.prefix = prefix;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            stack++;
            switch (step) {
                case other -> {
                    if ("section".equals(localName) && classAttributesToList(atts).containsAll(sectionOverviewClasses)) {
                        step = ParseType.overviewSection;
                    }
                }
                case overviewSection -> {
                    if ("div".equals(localName) && "teaser".equals(atts.getValue("class"))) {
                        step = ParseType.divTeaser;
                    }
                }
                case divTeaser -> {
                    if ("h2".equals(localName)) {
                        step = ParseType.href;
                    }
                }
                case href -> {
                    if ("a".equals(localName)) {
                        String url = atts.getValue("href");
                        if (url.startsWith(prefix)) {
                            urls.add(url);
                        } else {
                            urls.add(prefix + url);
                        }
                        step = ParseType.divTeaser;
                    }
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            stack--;
            switch (step) {
                case other -> super.endElement(uri, localName, qName);
                case divTeaser -> {
                    if ("section".equals(localName)) {
                        throw new EndOfProcessing();
                    }
                }
            }
        }

        List<String> classAttributesToList(Attributes atts) {
            String value = atts.getValue("class");
            if (StringUtils.isBlank(value)) {
                return Collections.emptyList();
            }
            List<String> result = new ArrayList<>();
            for (String v : value.split(" ")) {
                result.add(v);
            }
            return result;
        }
        
        protected void initStack() {
            ptStack = stack;
            characters.reset();
        }

        public List<String> getUrls() {
            return urls;
        }
        
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (step != ParseType.other) {
                characters.write(ch, start, length);
            }
        }

    }
}
