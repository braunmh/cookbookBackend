package org.braun.cookbook.backend.crawler;

import org.braun.cookbook.common.EndOfProcessing;
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
import java.util.Calendar;
import java.util.List;
import org.braun.cookbook.backend.model.BackgroundJobType;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author mbraun
 */
@Named("ArdNdrCrawler")
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ArdNdrCrawler extends AbstractArdNdrCrawler {
    
    @Override
    protected String getPathParent() {
        Calendar now = Calendar.getInstance();
        return "ARD/ndr/" + now.get(Calendar.YEAR);
    }
    
    @Override
    protected List<UrlString> getNewRecipes() {
        String prefix = "https://www.ndr.de";
        OverviewFilter filter = new OverviewFilter(prefix);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(prefix + getSite()))
                .GET()
                .build();
        
        try (HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
                InputStream inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();) {
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
        return filter.getUrls();
    }

    @Override
    public BackgroundJobType getTaskName() {
        return BackgroundJobType.ArdNdrCrawler;
    }

    protected String getSite() {
        return "/ratgeber/kochen";
    }
    
}
