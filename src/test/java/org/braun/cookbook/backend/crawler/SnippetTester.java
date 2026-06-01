package org.braun.cookbook.backend.crawler;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.braun.cookbook.backend.model.Recipe;
import org.ccil.cowan.tagsoup.Parser;
import org.junit.jupiter.api.Test;

/**
 *
 * @author mbraun
 */
public class SnippetTester {
    private static final String DIRECTORY = "org/braun/cookbook/backend/model/recipeLd/";
    
    @Test
    public void clean() {
        String value = "3. Die<strong> </strong>ausgedrückten&amp;nbsp;Kartoffelflüssigkeit in einen&amp;nbsp;Topf gießen. Diese wird fürs Dressing gebraucht<strong> (Wichtig</strong>: Dabei soll die am bodenhafte, verbleibende Kartoffelstärke für die Rösti verwendet werden, also in der Schüssel zurückbleiben)";
        value = value.replaceAll("<strong>", "");
        value = value.replaceAll("</strong>", "");
        value = value.replaceAll("&amp;nbsp;", " ");
        System.out.println(value);
    }
    
    @Test
    public void test() throws Exception {
        XyCrawler xy = new XyCrawler();
        System.out.println(xy.getUrls());
        for (String url : xy.getUrls()) {
            Recipe recipe = xy.getRecipe(url);
            StringWriter writer = new StringWriter();
            recipe.marshall(writer);
            System.out.println(writer.toString());
        }
    }
    @Test
    public void test1()   {
                String prefix = "https://www.lecker.de";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(prefix + "/tagesrezept"))
                .GET()
                .build();
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .version(HttpClient.Version.HTTP_2)
                .build();

        try (InputStream inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();
                GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
            HtmlSnippetReader snippetReader =  new HtmlSnippetReader(gzipInputStream, "<main", "</main>");) {
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
    
    class XyCrawler extends LeckerCrawler {
        public List<String> getUrls() {
            return getNewRecipes();
        }
        
    }
}
