package org.braun.cookbook.backend.crawler;

import java.io.CharArrayWriter;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.braun.cookbook.backend.crawler.gu.GuCrawler;
import org.braun.cookbook.backend.crawler.gu.UrlGu;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Usage Example:
 *
 * <pre>protected List<UrlGu> getNewRecipes() {
 *  String prefix = "https://www.kuechengoetter.de/rezepte.rss";
 *  HttpRequest request = HttpRequest.newBuilder()
 *   .uri(URI.create(prefix))
 *   .GET()
 *   .build();
 *  HttpClient client = HttpClient.newBuilder()
 *   .followRedirects(HttpClient.Redirect.NORMAL)
 *   .build();
 *  RssFilter rssFilter = new RssFilter(this);
 *  try (InputStream inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();) {
 *   SAXParserFactory spf = SAXParserFactory.newInstance();
 *   SAXParser sp = spf.newSAXParser();
 *   XMLReader reader = sp.getXMLReader();
 *   reader.setFeature(Parser.namespacePrefixesFeature, false);
 *   InputSource inputSource = new InputSource(inputStream);
 *   rssFilter.setParent(reader);
 *   rssFilter.parse(inputSource);
 *  } catch (SAXException | ParserConfigurationException | IOException | InterruptedException e) {
 *   LOG.error("execute failed with", e);
 *  }
 *  return rssFilter.getUrls();
 * }
 *
 *
 * </pre>
 *
 * @author mbraun
 */
public class RssFilter extends XMLFilterImpl {

    private static final Logger LOG = LogManager.getLogger();

    private final GuCrawler rssFilter;

    public RssFilter(final GuCrawler rssFilter) {
        this.rssFilter = rssFilter;
        urls = new ArrayList<>();
        step = Step.parse;
        writer = new CharArrayWriter();
    }

    enum Step {
        item, parse
    }
    List<UrlOffsetTime> urls;
    Step step;
    CharArrayWriter writer;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        switch (step) {
            case parse -> {
                if ("item".equals(qName)) {
                    step = Step.item;
                    urls.add(new UrlOffsetTime());
                }
            }
            case item -> {
                switch (qName) {
                    case "link", "pubDate" ->
                        writer.reset();
                }
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (step == Step.item) {
            switch (qName) {
                case "item" ->
                    step = Step.parse;
                case "link" ->
                    urls.getLast().setUrl(writer.toString());
                case "pubDate" ->
                    urls.getLast().setOffsetDateTime(getOdt(writer.toString()));
            }
        }
    }

    private OffsetDateTime getOdt(String value) {
        if (value == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME);
        } catch (DateTimeParseException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (step == Step.item) {
            writer.write(ch, start, length);
        }
    }

    public List<UrlOffsetTime> getUrls() {
        return urls;
    }

}
