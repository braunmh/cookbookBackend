package org.braun.cookbook.backend.crawler.gu;

import java.io.CharArrayWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.braun.cookbook.backend.crawler.CrawlerBase;
import org.braun.cookbook.backend.model.BackgroundJobType;
import org.braun.cookbook.backend.model.JsonFilter;
import org.braun.cookbook.backend.model.Recipe;
import org.braun.cookbook.backend.model.RecipeLd;
import static org.braun.cookbook.backend.model.RecipeLd.getRecipeFromJson;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 *
 * @author mbraun
 */
public class GuCrawler extends CrawlerBase<UrlGu> {

    @Override
    protected Recipe getRecipe(UrlGu url) {
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

            TitleFilter titleFilter = new TitleFilter();
            titleFilter.setParent(parser);

            JsonFilter jsonFilter = new JsonFilter();
            jsonFilter.setParent(titleFilter);
            jsonFilter.parse(inputSource);

            RecipeLd recipeLd = getRecipeFromJson(jsonFilter.getJson());
            if (recipeLd == null) {
                return null;
            }
            recipeLd.getRecipeCategory().addAll(new ArrayList<>(url.getKeyword()));
            Recipe recipe = toRecipe(recipeLd, "GU");
            if (titleFilter.title != null) {
                recipe.setTitle(titleFilter.title);
            }
            recipe.getSource().setUrl(url.getUrl());
            return recipe;
        } catch (SAXException | IOException | InterruptedException e) {
            LOG.error("execute failed with", e);
        }
        return null;
    }

    List<UrlGu> sites = List.of(
            // nach EIGENSCHAFTEN
            new UrlGu().keyword("Schnell").url("https://www.kuechengoetter.de/eigenschaften/schnell-169"),
            new UrlGu().keyword("Leicht").url("https://www.kuechengoetter.de/eigenschaften/leicht-163"),
            new UrlGu().keyword("Feste").url("https://www.kuechengoetter.de/eigenschaften/festliches-150"),
            new UrlGu().keyword("Vegan").url("https://www.kuechengoetter.de/eigenschaften/vegan-170"),
            new UrlGu().keyword("Vegetarisch").url("https://www.kuechengoetter.de/eigenschaften/vegetarisch-171"),
            new UrlGu().keyword("Preiswert").url("https://www.kuechengoetter.de/eigenschaften/preiswert-167"),
            new UrlGu().keyword("Vollwert").url("https://www.kuechengoetter.de/eigenschaften/vollwert-172"),
            new UrlGu().keyword("Laktosefrei").url("https://www.kuechengoetter.de/eigenschaften/laktosefrei-162"),
            new UrlGu().keyword("Alkoholfrei").url("https://www.kuechengoetter.de/eigenschaften/ohne-alkohol-165"),
            new UrlGu().keyword("Feste").url("https://www.kuechengoetter.de/eigenschaften/festliches-150"),
            // Gerichtyp
            new UrlGu().keyword("Alkoholfrei", "Drinks").url("https://www.kuechengoetter.de/gerichttyp/alkoholfreie-cocktails-drinks-23"),
            new UrlGu().keyword("Drinks").url("https://www.kuechengoetter.de/gerichttyp/cocktails-drinks-29"),
            new UrlGu().keyword("Brot").url("https://www.kuechengoetter.de/gerichttyp/brot-broetchen-26"),
            new UrlGu().keyword("Eis").url("https://www.kuechengoetter.de/gerichttyp/eis-33"),
            new UrlGu().keyword("Grundlagen").url("https://www.kuechengoetter.de/gerichttyp/grundrezept-241"),
            new UrlGu().keyword("Kuchen", "Backen").url("https://www.kuechengoetter.de/gerichttyp/kuchen-38"),
            new UrlGu().keyword("Mehlspeise").url("https://www.kuechengoetter.de/gerichttyp/mehlspeisen-42"),
            new UrlGu().keyword("Teigwaren").url("https://www.kuechengoetter.de/rs/pasta-und-nudel-rezepte"),
            new UrlGu().keyword("Pralinen").url("https://www.kuechengoetter.de/gerichttyp/pralinen-konfekt-50"),
            new UrlGu().keyword("Alkohol", "Drinks").url("https://www.kuechengoetter.de/gerichttyp/punsch-gluehwein-52"),
            new UrlGu().keyword("Raclette").url("https://www.kuechengoetter.de/gerichttyp/raclette-54"),
            new UrlGu().keyword("Sandwich").url("https://www.kuechengoetter.de/gerichttyp/sandwiches-brote-56"),
            new UrlGu().keyword("Drinks", "Alkoholfrei").url("https://www.kuechengoetter.de/gerichttyp/shakes-smoothies-58"),
            new UrlGu().keyword("Tee").url("https://www.kuechengoetter.de/gerichttyp/tee-62"),
            new UrlGu().keyword("Suppe").url("https://www.kuechengoetter.de/gerichttyp/suppen-60"),
            new UrlGu().keyword("Pizza").url("https://www.kuechengoetter.de/gerichttyp/pizza-48"),
            new UrlGu().keyword("Getränke", "Alkohol").url("https://www.kuechengoetter.de/gerichttyp/bowle-25"),
            new UrlGu().keyword("Braten").url("https://www.kuechengoetter.de/gerichttyp/braten-213"),
            new UrlGu().keyword("Brotaufstrich").url("https://www.kuechengoetter.de/gerichttyp/brotaufstrich-27"),
            new UrlGu().keyword("Chutney").url("https://www.kuechengoetter.de/gerichttyp/chutneys-pickles-28"),
            new UrlGu().keyword("Gebäck", "Kuchen").url("https://www.kuechengoetter.de/gerichttyp/cupcakes-30"),
            new UrlGu().keyword("Eintopf").url("https://www.kuechengoetter.de/gerichttyp/eintoepfe-32"),
            new UrlGu().keyword("Fondue").url("https://www.kuechengoetter.de/gerichttyp/fondue-34"),
            new UrlGu().keyword("Kaffee").url("https://www.kuechengoetter.de/gerichttyp/kaffee-35"),
            new UrlGu().keyword("Gebäck").url("https://www.kuechengoetter.de/gerichttyp/kleingebaeck-37"),
            new UrlGu().keyword("Likör", "Alkohol").url("https://www.kuechengoetter.de/gerichttyp/likoere-schnaepse-39"),
            new UrlGu().keyword("Brotaufstrich").url("https://www.kuechengoetter.de/rs/marmeladen"),
            new UrlGu().keyword("Gebäck", "Kuchen").url("https://www.kuechengoetter.de/gerichttyp/muffins-43"),
            new UrlGu().keyword("Pastete", "Terrine").url("https://www.kuechengoetter.de/gerichttyp/pasteten-terrinen-45"),
            new UrlGu().keyword("Pikant", "Eingelegt").url("https://www.kuechengoetter.de/gerichttyp/pikant-eingemachtes-47"),
            new UrlGu().keyword("Gebäck").url("https://www.kuechengoetter.de/gerichttyp/plaetzchen-kekse-49"),
            new UrlGu().keyword("Pudding").url("https://www.kuechengoetter.de/gerichttyp/pudding-cremes-51"),
            new UrlGu().keyword("Pastete").url("https://www.kuechengoetter.de/gerichttyp/quiche-53"),
            new UrlGu().keyword("Sauce").url("https://www.kuechengoetter.de/gerichttyp/saucen-57"),
            new UrlGu().keyword("Salat").url("https://www.kuechengoetter.de/gerichttyp/salate-55"),
            new UrlGu().keyword("Sirup").url("https://www.kuechengoetter.de/gerichttyp/sirup-59"),
            new UrlGu().keyword("Eingelegt").url("https://www.kuechengoetter.de/einmachen/suess-einmachen"),
            new UrlGu().keyword("Backen", "Kuchen").url("https://www.kuechengoetter.de/gerichttyp/torten-63"),
            new UrlGu().keyword("Gewürze").url("https://www.kuechengoetter.de/gerichttyp/wuerzmittel-65"),
            new UrlGu().keyword("Auflauf").url("https://www.kuechengoetter.de/gerichttyp/auflauf-24"),
// Nach Zutaten
            new UrlGu().keyword("Obst").url("https://www.kuechengoetter.de/zutaten/mirabellen-130"),
            new UrlGu().keyword("Obst").url("https://www.kuechengoetter.de/rs/quitten-rezepte"),
            new UrlGu().keyword("Obst").url("https://www.kuechengoetter.de/rs/apfel-rezepte"),
            new UrlGu().keyword("Gemüse").url("https://www.kuechengoetter.de/zutaten/pastinaken-192"),
            new UrlGu().keyword("Gemüse", "Kohl").url("https://www.kuechengoetter.de/zutaten/rosenkohl-154"),
            new UrlGu().keyword("Gemüse").url("https://www.kuechengoetter.de/zutaten/topinambur-190"),
            new UrlGu().keyword("Gemüse").url("https://www.kuechengoetter.de/zutaten/avocados-128"),
            new UrlGu().keyword("Kartoffeln").url("https://www.kuechengoetter.de/zutaten/suesskartoffeln-230"),
            new UrlGu().keyword("Kohl").url("https://www.kuechengoetter.de/zutaten/pak-choi-83"),
            new UrlGu().keyword("Kürbis").url("https://www.kuechengoetter.de/zutaten/kuerbis-75"),
            new UrlGu().keyword("Gemüse").url("https://www.kuechengoetter.de/zutaten/fenchel-108"),
// nach Länder und Regionen
            new UrlGu().keyword("Italien").url("https://www.kuechengoetter.de/region/italien-111"),
            new UrlGu().keyword("Süditalien").url("https://www.kuechengoetter.de/region/italien-sueditalien-114"),
            new UrlGu().keyword("Sizilien").url("https://www.kuechengoetter.de/region/italien-sizilien-113"),
            new UrlGu().keyword("Umbrien").url("https://www.kuechengoetter.de/region/italien-umbrien-116"),
            new UrlGu().keyword("Toskana").url("https://www.kuechengoetter.de/region/italien-toskana-115"),
            new UrlGu().keyword("Norditalien").url("https://www.kuechengoetter.de/region/italien-norditalien-112"),
            new UrlGu().keyword("USA").url("https://www.kuechengoetter.de/region/amerika-usa-79"),
            new UrlGu().keyword("Mexiko").url("https://www.kuechengoetter.de/region/amerika-mexiko-77"),
            new UrlGu().keyword("Südamerika").url("https://www.kuechengoetter.de/region/amerika-suedamerika-78"),
            new UrlGu().keyword("Amerika").url("https://www.kuechengoetter.de/region/amerika-76"),
            new UrlGu().keyword("Asien").url("https://www.kuechengoetter.de/region/asien-80"),
            new UrlGu().keyword("Vietnam").url("https://www.kuechengoetter.de/region/asien-vietnam-86"),
            new UrlGu().keyword("Thailand").url("https://www.kuechengoetter.de/region/asien-thailand-85"),
            new UrlGu().keyword("Indonesien").url("https://www.kuechengoetter.de/region/asien-indonesien-83"),
            new UrlGu().keyword("Indien").url("https://www.kuechengoetter.de/region/asien-indien-82"),
            new UrlGu().keyword("China").url("https://www.kuechengoetter.de/region/asien-china-81"),
            new UrlGu().keyword("Japan").url("https://www.kuechengoetter.de/region/asien-japan-84"),
           
            new UrlGu().keyword("Deutschland").url("https://www.kuechengoetter.de/region/deutschland-88"),
            new UrlGu().keyword("Bremen").url("https://www.kuechengoetter.de/region/deutschland-bremen-92"),
            new UrlGu().keyword("Brandenburg").url("https://www.kuechengoetter.de/region/deutschland-brandenburg-91"),
            new UrlGu().keyword("Berlin").url("https://www.kuechengoetter.de/region/deutschland-berlin-90"),
            new UrlGu().keyword("Bayern").url("https://www.kuechengoetter.de/region/deutschland-bayern-87"),
            new UrlGu().keyword("Baden-Württenberg").url("https://www.kuechengoetter.de/region/deutschland-baden-wuerttemberg-89"),
            new UrlGu().keyword("Hamburg").url("https://www.kuechengoetter.de/region/deutschland-hamburg-93"),
            new UrlGu().keyword("Hessen").url("https://www.kuechengoetter.de/region/deutschland-hessen-94"),
            new UrlGu().keyword("Mecklenburg").url("https://www.kuechengoetter.de/region/deutschland-mecklenburg-vorpommern-95"),
            new UrlGu().keyword("Norddeutschland").url("https://www.kuechengoetter.de/region/deutschland-norddeutschland-97"),
            new UrlGu().keyword("Niedersachsen").url("https://www.kuechengoetter.de/region/deutschland-niedersachsen-96"),
            new UrlGu().keyword("Nordrhein-Westfalen").url("https://www.kuechengoetter.de/region/deutschland-nordrhein-westfalen-98"),
            new UrlGu().keyword("Saarland").url("https://www.kuechengoetter.de/region/deutschland-saarland-100"),
            new UrlGu().keyword("Rheinland-Pfalz").url("https://www.kuechengoetter.de/region/deutschland-rheinland-pfalz-99"),
            new UrlGu().keyword("Sachsen").url("https://www.kuechengoetter.de/region/deutschland-sachsen-101"),
            new UrlGu().keyword("Sachsen-Anhalt").url("https://www.kuechengoetter.de/region/deutschland-sachsen-anhalt-102"),
            new UrlGu().keyword("Schleswig-Holstein").url("https://www.kuechengoetter.de/region/deutschland-schleswig-holstein-103"),
            new UrlGu().keyword("Süddeutschland").url("https://www.kuechengoetter.de/region/deutschland-sueddeutschland-104"),
            new UrlGu().keyword("Thüringen").url("https://www.kuechengoetter.de/region/deutschland-thueringen-105"),
            new UrlGu().keyword("Nordrhein-Westfalen").url("https://www.kuechengoetter.de/region/deutschland-nordrhein-westfalen-98"),
            new UrlGu().keyword("Norddeutschland").url("https://www.kuechengoetter.de/region/deutschland-norddeutschland-97"),
            new UrlGu().keyword("Niedersachsen").url("https://www.kuechengoetter.de/region/deutschland-niedersachsen-96"),
            new UrlGu().keyword("Frankreich").url("https://www.kuechengoetter.de/region/frankreich-107"),
            new UrlGu().keyword("Nordfrankreich").url("https://www.kuechengoetter.de/region/frankreich-nordfrankreich-108"),
            new UrlGu().keyword("Südfrankreich").url("https://www.kuechengoetter.de/region/frankreich-suedfrankreich-109"),
            new UrlGu().keyword("Orient").url("https://www.kuechengoetter.de/region/orient-afrika-120"),
            new UrlGu().keyword("Libanon").url("https://www.kuechengoetter.de/region/orient-afrika-libanon-117"),
            new UrlGu().keyword("Marokko").url("https://www.kuechengoetter.de/region/orient-afrika-marokko-119"),
            new UrlGu().keyword("Israel").url("https://www.kuechengoetter.de/region/israel-589"),
            new UrlGu().keyword("England").url("https://www.kuechengoetter.de/region/england-grossbritannien-106"),
            new UrlGu().keyword("Griechenland").url("https://www.kuechengoetter.de/region/griechenland-110"),
            new UrlGu().keyword("Russland").url("https://www.kuechengoetter.de/region/russland-122"),
            new UrlGu().keyword("Polen").url("https://www.kuechengoetter.de/region/skandinavien-124"),
            new UrlGu().keyword("Skandinavien").url("https://www.kuechengoetter.de/region/skandinavien-124"),
            new UrlGu().keyword("Schweiz").url("https://www.kuechengoetter.de/region/schweiz-123"),
            new UrlGu().keyword("Türkei").url("https://www.kuechengoetter.de/region/tuerkei-126"),
            new UrlGu().keyword("Spanien").url("https://www.kuechengoetter.de/region/spanien-125"),
            new UrlGu().keyword("Österreich").url("https://www.kuechengoetter.de/region/oesterreich-128"),
            new UrlGu().keyword("Ungarn").url("https://www.kuechengoetter.de/region/ungarn-127"),
// passend zu Saison & Anlässen
            new UrlGu().keyword("Frühling").url("https://www.kuechengoetter.de/saison-oder-anlass/fruehling-132"),
            new UrlGu().keyword("Sommer").url("https://www.kuechengoetter.de/saison-oder-anlass/sommer-144"),
            new UrlGu().keyword("Herbst").url("https://www.kuechengoetter.de/saison-oder-anlass/herbst-134"),
            new UrlGu().keyword("Winter").url("https://www.kuechengoetter.de/saison-oder-anlass/winter-147"),
            new UrlGu().keyword("Weihnachten").url("https://www.kuechengoetter.de/saison-oder-anlass/weihnachten-146"),
            new UrlGu().keyword("Halloween").url("https://www.kuechengoetter.de/saison-oder-anlass/halloween-133"),
            new UrlGu().keyword("Hochzeit").url("https://www.kuechengoetter.de/saison-oder-anlass/hochzeit-135"),
            new UrlGu().keyword("Karneval").url("https://www.kuechengoetter.de/saison-oder-anlass/karneval-fasching-136"),
            new UrlGu().keyword("Kinderfeste").url("https://www.kuechengoetter.de/saison-oder-anlass/kinderfeste-137"),
            new UrlGu().keyword("Muttertag").url("https://www.kuechengoetter.de/saison-oder-anlass/muttertag-138"),
            new UrlGu().keyword("Ostern").url("https://www.kuechengoetter.de/saison-oder-anlass/ostern-139"),
            new UrlGu().keyword("Party").url("https://www.kuechengoetter.de/saison-oder-anlass/party-buefett-140"),
            new UrlGu().keyword("Picknick").url("https://www.kuechengoetter.de/saison-oder-anlass/picknick-141"),
            new UrlGu().keyword("Silvester").url("https://www.kuechengoetter.de/saison-oder-anlass/silvester-neujahr-143"),
            new UrlGu().keyword("Valentinstag").url("https://www.kuechengoetter.de/saison-oder-anlass/valentinstag-145"),
// Rezepte nach Art der Zubereitung
            new UrlGu().keyword("Backen").url("https://www.kuechengoetter.de/art-der-zubereitung/aus-dem-backofen-6"),
            new UrlGu().keyword("Brot").url("https://www.kuechengoetter.de/art-der-zubereitung/aus-dem-brotbackautomat-7"),
            new UrlGu().keyword("Römertopf").url("https://www.kuechengoetter.de/art-der-zubereitung/aus-dem-roemertopf-8"),
            new UrlGu().keyword("Mikrowelle").url("https://www.kuechengoetter.de/art-der-zubereitung/aus-der-mikrowelle-9"),
            new UrlGu().keyword("Pfannengerichte").url("https://www.kuechengoetter.de/art-der-zubereitung/aus-der-pfanne-10"),
            new UrlGu().keyword("Backen").url("https://www.kuechengoetter.de/art-der-zubereitung/backen-11"),
            new UrlGu().keyword("Braten").url("https://www.kuechengoetter.de/art-der-zubereitung/braten-12"),
            new UrlGu().keyword("Dampfgaren").url("https://www.kuechengoetter.de/art-der-zubereitung/dampfgaren-13"),
            new UrlGu().keyword("Dämpfen").url("https://www.kuechengoetter.de/art-der-zubereitung/daempfen-14"),
            new UrlGu().keyword("Dünsten").url("https://www.kuechengoetter.de/art-der-zubereitung/duensten-15"),
            new UrlGu().keyword("Frittieren").url("https://www.kuechengoetter.de/art-der-zubereitung/frittieren-16"),
            new UrlGu().keyword("Thermomix").url("https://www.kuechengoetter.de/art-der-zubereitung/fuer-den-thermomix-248"),
            new UrlGu().keyword("Grillen").url("https://www.kuechengoetter.de/art-der-zubereitung/grillen-17"),
            new UrlGu().keyword("Kochen").url("https://www.kuechengoetter.de/art-der-zubereitung/kochen-18"),
            new UrlGu().keyword("Niedrigtemperaturgaren").url("https://www.kuechengoetter.de/art-der-zubereitung/niedrigtemperaturgaren-19"),
            new UrlGu().keyword("Schmoren").url("https://www.kuechengoetter.de/art-der-zubereitung/schmoren-20"),
            new UrlGu().keyword("Tajine").url("https://www.kuechengoetter.de/art-der-zubereitung/tajine-247"),
            new UrlGu().keyword("Wokken").url("https://www.kuechengoetter.de/art-der-zubereitung/wokken-21"),
// Menüfolge - Rezepte für jeden Gang
            new UrlGu().keyword("Beilage").url("https://www.kuechengoetter.de/menuefolge/beilage-67"),
            new UrlGu().keyword("Brunch").url("https://www.kuechengoetter.de/menuefolge/brunch-fruehstueck-68"),
            new UrlGu().keyword("Drinks").url("https://www.kuechengoetter.de/menuefolge/cocktails-drinks-215"),
            new UrlGu().keyword("Dessert").url("https://www.kuechengoetter.de/menuefolge/dessert-69"),
            new UrlGu().keyword("Hauptspeise").url("https://www.kuechengoetter.de/menuefolge/hauptspeise-70"),
            new UrlGu().keyword("Fingerfood").url("https://www.kuechengoetter.de/menuefolge/haeppchen-fingerfood-71"),
            new UrlGu().keyword("Vorspeise").url("https://www.kuechengoetter.de/menuefolge/vorspeise-74"),
            new UrlGu().keyword("Snack").url("https://www.kuechengoetter.de/menuefolge/snacks-72"),
            new UrlGu().keyword("Süßspeise").url("https://www.kuechengoetter.de/menuefolge/suesse-hauptspeise-73")
    );

    @Override
    protected List<UrlGu> getNewRecipes() {
        
        Map<String, UrlGu> result = new HashMap<>();
        String outputDirectory = "/opt/solr/data/cookbook/gu/";
        int s = 0;
        for (UrlGu url : sites) {
            String tempNews = outputDirectory + "news" + s++ % 2 + ".txt";
            int currentPage = 0;
            try (FileWriter fw = new FileWriter(tempNews)) {
                OverviewFilter filter = getRecipes(url.getUrl(), 1);
                int countOfPages = filter.getCountPages();
                for (String v : filter.getUrls()) {
                    UrlGu u = result.get(v);
                    if (u == null) {
                       result.put(v, new UrlGu().keyword(url.getKeyword()).url(v));
                    } else {
                        u.keyword(url.getKeyword());
                    }
                }

                for (currentPage = 2; currentPage < countOfPages + 1; currentPage++) {
                    filter = getRecipes(url.getUrl(), currentPage);
                    for (String v : filter.getUrls()) {
                        UrlGu u = result.get(v);
                        if (u == null) {
                           result.put(v, new UrlGu().keyword(url.getKeyword()).url(v));
                        } else {
                            u.keyword(url.getKeyword());
                        }
                    }
//                    if (currentPage > 2) {
//                        break;
//                    }
                }
                fw.write(url.getUrl() + "\n");
                for (UrlGu ug : result.values()) {
                    fw.write(ug.getUrl() + "\t" + String.join("\t", ug.getKeyword()) + "\n");
                }
            } catch (IOException e) {
                System.out.println("Error processing site " + url.getUrl() + ", page = " + currentPage);
                e.printStackTrace(System.out);
            }
//            if (s > 4) {
//                break;
//            }
        }
        return new ArrayList<>(result.values());
    }

    
    private OverviewFilter getRecipes(String url, int count) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "?seite=" + count))
                .GET()
                .build();
        OverviewFilter overviewFilter = new OverviewFilter(url);
        StringWriter sw = new StringWriter();
        try (HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build(); InputStream inputStream = client.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();) {
            XMLReader reader = new Parser();
            reader.setFeature(Parser.namespacePrefixesFeature, false);
            InputSource inputSource = new InputSource(inputStream);
            overviewFilter.setParent(reader);
            overviewFilter.parse(inputSource);
        } catch (SAXException | IOException | InterruptedException e) {
            LOG.error("execute failed with", e);
        }
        return overviewFilter;
    }

    @Override
    protected String getPathParent(Recipe recipe, UrlGu url) {
        if (null != recipe.getPublished() && recipe.getPublished() > 0) {
            LocalDateTime ldt = LocalDateTime.ofEpochSecond(recipe.getPublished() / 1000, 0, ZoneOffset.UTC);
            return "GU/" + ldt.getYear();
        } else {
            return String.format("GU/2026/%04d", url.getCount() % 100);
        }
    }

    @Override
    public BackgroundJobType getTaskName() {
        return BackgroundJobType.GuCrawler;
    }
    
    class TitleFilter extends XMLFilterImpl {
        
        boolean hasTitle = false;
        String title;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if (!hasTitle) {
                if ("meta".equals(qName) && "DC.Title".equals(atts.getValue("name")) ) {
                    title = atts.getValue("content");
                    hasTitle = true;
                }
            }
            super.startElement(uri, localName, qName, atts);
        }
        
    }

    class OverviewFilter extends XMLFilterImpl {

        enum Step {
            parse, main, section, article, nav, finish
        };

        private int countPages;

        private final Set<String> urls;

        private final List<String> navLinks;

        private Step step;

        private final CharArrayWriter writer;

        private final String urlParsed;

        public OverviewFilter(String urlParsed) {
            countPages = 0;
            urls = new HashSet<>();
            step = Step.parse;
            writer = new CharArrayWriter();
            navLinks = new ArrayList<>();
            this.urlParsed = urlParsed;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            switch (step) {
                case parse -> {
                    if (qName.equals("main")) {
                        step = Step.main;
                    }
                }
                case main -> {
                    if ("section".equals(qName)) {
                        step = Step.section;
                    } else if ("nav".equals(qName)) {
                        step = Step.nav;
                    }
                }
                case section -> {
                    if ("article".equals(qName)) {
                        step = Step.article;
                    }
                }
                case article -> {
                    if ("a".equals(qName)) {
                        String href = atts.getValue("href");
                        if (href != null) {
                            urls.add(href);
                        }
                    }
                }
                case nav -> {
                    if ("a".equals(qName)) {
                        writer.reset();
                    }
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (step) {
                case main -> {
                    if ("main".equals(qName)) {
                        step = Step.finish;
                    }
                }
                case article -> {
                    if ("article".equals(qName)) {
                        step = Step.section;
                    }
                }
                case section -> {
                    if ("section".equals(qName)) {
                        step = Step.main;
                    }
                }
                case nav -> {
                    if ("nav".equals(qName)) {
                        step = Step.main;
                        if (navLinks.isEmpty() || navLinks.size() < 2) {
                            countPages = 1;
                        } else {
                            try {
                                countPages = Integer.parseInt(navLinks.get(navLinks.size() - 2));
                            } catch (NumberFormatException e) {
                                LOG.error("{} error getting count of pages: {}", urlParsed, e.getMessage());
                            }
                        }
                    } else if ("a".equals(qName)) {
                        navLinks.add(writer.toString());
                        writer.reset();
                    }
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (step == Step.nav) {
                writer.write(ch, start, length);
            }
        }

        public int getCountPages() {
            return countPages;
        }

        public Set<String> getUrls() {
            return urls;
        }

    }
}
