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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.zip.GZIPInputStream;
import static org.braun.cookbook.backend.crawler.Crawler.LOG;
import org.braun.cookbook.backend.model.BackgroundJobType;
import org.braun.cookbook.backend.model.JsonFilter;
import org.braun.cookbook.backend.model.Recipe;
import org.braun.cookbook.backend.model.RecipeLd;
import static org.braun.cookbook.backend.model.RecipeLd.getRecipeFromJson;
import org.braun.cookbook.backend.model.recipe.Paragraph;
import org.braun.cookbook.backend.model.recipe.StructureElement;
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
public class LeckerCrawler extends Crawler {

    @Override
    protected Recipe getRecipe(String url) {
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        try (InputStream inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();
             GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);) {
            Parser parser = new Parser();
            parser.setFeature(Parser.namespacePrefixesFeature, false);
            InputSource inputSource = new InputSource(gzipInputStream);

            JsonFilter jsonFilter = new JsonFilter();
            jsonFilter.setParent(parser);

            jsonFilter.parse(inputSource);

            RecipeLd recipeLd = getRecipeFromJson(jsonFilter.getJson());
            if (recipeLd == null) {
                return null;
            }
            Recipe recipe = recipeLd.toRecipe();
            for (StructureElement line : recipe.getDescription().getContent()) {
                if (line instanceof Paragraph paragraph) {
                    String para = paragraph.getValue().replaceAll("&amp;nbsp;", " ");
                    para = para.replaceAll("&amp;#40;", "(");
                    para = para.replaceAll("&amp;#41;", ")");
                    para = para.replaceAll("&nbsp;", " ");
                    paragraph.setValue(para);
                }
            }
            return recipe;
        } catch (SAXException | IOException | InterruptedException e) {
            LOG.error("execute failed with", e);
        }
        return null;
    }

    @Override
    protected String getPathParent() {
        Calendar now = Calendar.getInstance();
        return "lecker/" + now.get(Calendar.YEAR);
    }

    @Override
    protected List<String> getNewRecipes() {
        String prefix = "https://www.lecker.de";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(prefix + "/tagesrezept"))
                .GET()
                .build();
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .version(HttpClient.Version.HTTP_2)
                .build();
        OverviewFilter overviewFilter = new OverviewFilter(prefix);
        try (InputStream inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();
            GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
            HtmlSnippetReader snippetReader =  new HtmlSnippetReader(gzipInputStream, "<main", "</main>");) {
            Parser reader = new Parser();
            reader.setFeature(Parser.namespacePrefixesFeature, false);
            InputSource inputSource = new InputSource(snippetReader);
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
        return BackgroundJobType.LeckerCrawler;
    }
    
    class OverviewFilter extends XMLFilterImpl {
        
        enum Step {
            other, main, finished;
        }
        private String prefix;
        private Step step;
        
        List<String> urls;
        int stack;
        
        public OverviewFilter(String prefix) {
            urls = new ArrayList<>();
            stack = 0;
            step = Step.other;
            this.prefix = prefix;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            stack++;
            switch (step) {
                case other -> {
                    if ("main".equals(localName)) {
                        step = Step.main;
                        stack = 0;
                    }
                }
                case main -> {
                    if ("a".equals(localName)) {
                        if ("teaser".equals(atts.getValue("data-tc"))) {
                            urls.add(prefix + atts.getValue("href"));
                        }
                    }
                }
            }
            super.startElement(uri, localName, qName, atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            stack--;
            super.endElement(uri, localName, qName);
        }

        public List<String> getUrls() {
            return urls;
        }
        
    }
    
}
