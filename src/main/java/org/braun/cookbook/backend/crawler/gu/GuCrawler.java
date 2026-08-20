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
            new UrlGu("https://www.kuechengoetter.de/eigenschaften/schnell-169").keyword("Schnell"),
            new UrlGu("https://www.kuechengoetter.de/eigenschaften/leicht-163").keyword("Leicht"),
            new UrlGu("https://www.kuechengoetter.de/eigenschaften/festliches-150").keyword("Feste"),
            new UrlGu("https://www.kuechengoetter.de/eigenschaften/vegan-170").keyword("Vegan"),
            new UrlGu("https://www.kuechengoetter.de/eigenschaften/vegetarisch-171").keyword("Vegetarisch"),
            new UrlGu("https://www.kuechengoetter.de/eigenschaften/preiswert-167").keyword("Preiswert"),
            new UrlGu("https://www.kuechengoetter.de/eigenschaften/vollwert-172").keyword("Vollwert"),
            new UrlGu("https://www.kuechengoetter.de/eigenschaften/laktosefrei-162").keyword("Laktosefrei"),
            new UrlGu("https://www.kuechengoetter.de/eigenschaften/ohne-alkohol-165").keyword("Alkoholfrei"),
            new UrlGu("https://www.kuechengoetter.de/eigenschaften/festliches-150").keyword("Feste"),
                        // Gerichtyp
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/alkoholfreie-cocktails-drinks-23").keyword("Alkoholfrei", "Drinks"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/cocktails-drinks-29").keyword("Drinks"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/brot-broetchen-26").keyword("Brot"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/eis-33").keyword("Eis"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/grundrezept-241").keyword("Grundlagen"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/kuchen-38").keyword("Kuchen", "Backen"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/mehlspeisen-42").keyword("Mehlspeise"),
            new UrlGu("https://www.kuechengoetter.de/rs/pasta-und-nudel-rezepte").keyword("Teigwaren"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/pralinen-konfekt-50").keyword("Pralinen"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/punsch-gluehwein-52").keyword("Alkohol", "Drinks"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/raclette-54").keyword("Raclette"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/sandwiches-brote-56").keyword("Sandwich"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/shakes-smoothies-58").keyword("Drinks", "Alkoholfrei"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/tee-62").keyword("Tee"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/suppen-60").keyword("Suppe"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/pizza-48").keyword("Pizza"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/bowle-25").keyword("Getränke", "Alkohol"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/braten-213").keyword("Braten"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/brotaufstrich-27").keyword("Brotaufstrich"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/chutneys-pickles-28").keyword("Chutney"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/cupcakes-30").keyword("Gebäck", "Kuchen"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/eintoepfe-32").keyword("Eintopf"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/fondue-34").keyword("Fondue"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/kaffee-35").keyword("Kaffee"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/kleingebaeck-37").keyword("Gebäck"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/likoere-schnaepse-39").keyword("Likör", "Alkohol"),
            new UrlGu("https://www.kuechengoetter.de/rs/marmeladen").keyword("Brotaufstrich"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/muffins-43").keyword("Gebäck", "Kuchen"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/pasteten-terrinen-45").keyword("Pastete", "Terrine"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/pikant-eingemachtes-47").keyword("Pikant", "Eingelegt"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/plaetzchen-kekse-49").keyword("Gebäck"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/pudding-cremes-51").keyword("Pudding"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/quiche-53").keyword("Pastete"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/saucen-57").keyword("Sauce"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/salate-55").keyword("Salat"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/sirup-59").keyword("Sirup"),
            new UrlGu("https://www.kuechengoetter.de/einmachen/suess-einmachen").keyword("Eingelegt"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/torten-63").keyword("Backen", "Kuchen"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/wuerzmittel-65").keyword("Gewürze"),
            new UrlGu("https://www.kuechengoetter.de/gerichttyp/auflauf-24").keyword("Auflauf"),
            // Nach Zutaten
            new UrlGu("https://www.kuechengoetter.de/zutaten/mirabellen-130").keyword("Obst"),
            new UrlGu("https://www.kuechengoetter.de/rs/quitten-rezepte").keyword("Obst"),
            new UrlGu("https://www.kuechengoetter.de/rs/apfel-rezepte").keyword("Obst"),
            new UrlGu("https://www.kuechengoetter.de/zutaten/pastinaken-192").keyword("Gemüse"),
            new UrlGu("https://www.kuechengoetter.de/zutaten/rosenkohl-154").keyword("Gemüse", "Kohl"),
            new UrlGu("https://www.kuechengoetter.de/zutaten/topinambur-190").keyword("Gemüse"),
            new UrlGu("https://www.kuechengoetter.de/zutaten/avocados-128").keyword("Gemüse"),
            new UrlGu("https://www.kuechengoetter.de/zutaten/suesskartoffeln-230").keyword("Kartoffeln"),
            new UrlGu("https://www.kuechengoetter.de/zutaten/pak-choi-83").keyword("Kohl"),
            new UrlGu("https://www.kuechengoetter.de/zutaten/kuerbis-75").keyword("Kürbis"),
            new UrlGu("https://www.kuechengoetter.de/zutaten/fenchel-108").keyword("Gemüse"),
            // nach Länder und Regionen
            new UrlGu("https://www.kuechengoetter.de/region/italien-111").keyword("Italien"),
            new UrlGu("https://www.kuechengoetter.de/region/italien-sueditalien-114").keyword("Süditalien"),
            new UrlGu("https://www.kuechengoetter.de/region/italien-sizilien-113").keyword("Sizilien"),
            new UrlGu("https://www.kuechengoetter.de/region/italien-umbrien-116").keyword("Umbrien"),
            new UrlGu("https://www.kuechengoetter.de/region/italien-toskana-115").keyword("Toskana"),
            new UrlGu("https://www.kuechengoetter.de/region/italien-norditalien-112").keyword("Norditalien"),
            new UrlGu("https://www.kuechengoetter.de/region/amerika-usa-79").keyword("USA"),
            new UrlGu("https://www.kuechengoetter.de/region/amerika-mexiko-77").keyword("Mexiko"),
            new UrlGu("https://www.kuechengoetter.de/region/amerika-suedamerika-78").keyword("Südamerika"),
            new UrlGu("https://www.kuechengoetter.de/region/amerika-76").keyword("Amerika"),
            new UrlGu("https://www.kuechengoetter.de/region/asien-80").keyword("Asien"),
            new UrlGu("https://www.kuechengoetter.de/region/asien-vietnam-86").keyword("Vietnam"),
            new UrlGu("https://www.kuechengoetter.de/region/asien-thailand-85").keyword("Thailand"),
            new UrlGu("https://www.kuechengoetter.de/region/asien-indonesien-83").keyword("Indonesien"),
            new UrlGu("https://www.kuechengoetter.de/region/asien-indien-82").keyword("Indien"),
            new UrlGu("https://www.kuechengoetter.de/region/asien-china-81").keyword("China"),
            new UrlGu("https://www.kuechengoetter.de/region/asien-japan-84").keyword("Japan"),

            new UrlGu("https://www.kuechengoetter.de/region/deutschland-88").keyword("Deutschland"),
            new UrlGu("https://www.kuechengoetter.de/region/deutschland-bremen-92").keyword("Bremen"),
            new UrlGu("https://www.kuechengoetter.de/region/deutschland-brandenburg-91").keyword("Brandenburg"),
            new UrlGu("https://www.kuechengoetter.de/region/deutschland-berlin-90").keyword("Berlin"),
            new UrlGu("https://www.kuechengoetter.de/region/deutschland-bayern-87").keyword("Bayern"),
            new UrlGu("https://www.kuechengoetter.de/region/deutschland-baden-wuerttemberg-89").keyword("Baden-Württenberg"),
            new UrlGu("https://www.kuechengoetter.de/region/deutschland-hamburg-93").keyword("Hamburg"),
            new UrlGu("https://www.kuechengoetter.de/region/deutschland-hessen-94").keyword("Hessen"),
            new UrlGu("https://www.kuechengoetter.de/region/deutschland-mecklenburg-vorpommern-95").keyword("Mecklenburg"),
            new UrlGu("https://www.kuechengoetter.de/region/deutschland-norddeutschland-97").keyword("Norddeutschland"),
            new UrlGu("https://www.kuechengoetter.de/region/deutschland-niedersachsen-96").keyword("Niedersachsen"),
            new UrlGu("https://www.kuechengoetter.de/region/deutschland-nordrhein-westfalen-98").keyword("Nordrhein-Westfalen"),
            new UrlGu("https://www.kuechengoetter.de/region/deutschland-saarland-100").keyword("Saarland"),
            new UrlGu("https://www.kuechengoetter.de/region/deutschland-rheinland-pfalz-99").keyword("Rheinland-Pfalz"),
            new UrlGu("https://www.kuechengoetter.de/region/deutschland-sachsen-101").keyword("Sachsen"),
            new UrlGu("https://www.kuechengoetter.de/region/deutschland-sachsen-anhalt-102").keyword("Sachsen-Anhalt"),
            new UrlGu("https://www.kuechengoetter.de/region/deutschland-schleswig-holstein-103").keyword("Schleswig-Holstein"),
            new UrlGu("https://www.kuechengoetter.de/region/deutschland-sueddeutschland-104").keyword("Süddeutschland"),
            new UrlGu("https://www.kuechengoetter.de/region/deutschland-thueringen-105").keyword("Thüringen"),
            new UrlGu("https://www.kuechengoetter.de/region/deutschland-nordrhein-westfalen-98").keyword("Nordrhein-Westfalen"),
            new UrlGu("https://www.kuechengoetter.de/region/deutschland-norddeutschland-97").keyword("Norddeutschland"),
            new UrlGu("https://www.kuechengoetter.de/region/deutschland-niedersachsen-96").keyword("Niedersachsen"),
            new UrlGu("https://www.kuechengoetter.de/region/frankreich-107").keyword("Frankreich"),
            new UrlGu("https://www.kuechengoetter.de/region/frankreich-nordfrankreich-108").keyword("Nordfrankreich"),
            new UrlGu("https://www.kuechengoetter.de/region/frankreich-suedfrankreich-109").keyword("Südfrankreich"),
            new UrlGu("https://www.kuechengoetter.de/region/orient-afrika-120").keyword("Orient"),
            new UrlGu("https://www.kuechengoetter.de/region/orient-afrika-libanon-117").keyword("Libanon"),
            new UrlGu("https://www.kuechengoetter.de/region/orient-afrika-marokko-119").keyword("Marokko"),
            new UrlGu("https://www.kuechengoetter.de/region/israel-589").keyword("Israel"),
            new UrlGu("https://www.kuechengoetter.de/region/england-grossbritannien-106").keyword("England"),
            new UrlGu("https://www.kuechengoetter.de/region/griechenland-110").keyword("Griechenland"),
            new UrlGu("https://www.kuechengoetter.de/region/russland-122").keyword("Russland"),
            new UrlGu("https://www.kuechengoetter.de/region/skandinavien-124").keyword("Polen"),
            new UrlGu("https://www.kuechengoetter.de/region/skandinavien-124").keyword("Skandinavien"),
            new UrlGu("https://www.kuechengoetter.de/region/schweiz-123").keyword("Schweiz"),
            new UrlGu("https://www.kuechengoetter.de/region/tuerkei-126").keyword("Türkei"),
            new UrlGu("https://www.kuechengoetter.de/region/spanien-125").keyword("Spanien"),
            new UrlGu("https://www.kuechengoetter.de/region/oesterreich-128").keyword("Österreich"),
            new UrlGu("https://www.kuechengoetter.de/region/ungarn-127").keyword("Ungarn"),
            // passend zu Saison & Anlässen
            new UrlGu("https://www.kuechengoetter.de/saison-oder-anlass/fruehling-132").keyword("Frühling"),
            new UrlGu("https://www.kuechengoetter.de/saison-oder-anlass/sommer-144").keyword("Sommer"),
            new UrlGu("https://www.kuechengoetter.de/saison-oder-anlass/herbst-134").keyword("Herbst"),
            new UrlGu("https://www.kuechengoetter.de/saison-oder-anlass/winter-147").keyword("Winter"),
            new UrlGu("https://www.kuechengoetter.de/saison-oder-anlass/weihnachten-146").keyword("Weihnachten"),
            new UrlGu("https://www.kuechengoetter.de/saison-oder-anlass/halloween-133").keyword("Halloween"),
            new UrlGu("https://www.kuechengoetter.de/saison-oder-anlass/hochzeit-135").keyword("Hochzeit"),
            new UrlGu("https://www.kuechengoetter.de/saison-oder-anlass/karneval-fasching-136").keyword("Karneval"),
            new UrlGu("https://www.kuechengoetter.de/saison-oder-anlass/kinderfeste-137").keyword("Kinderfeste"),
            new UrlGu("https://www.kuechengoetter.de/saison-oder-anlass/muttertag-138").keyword("Muttertag"),
            new UrlGu("https://www.kuechengoetter.de/saison-oder-anlass/ostern-139").keyword("Ostern"),
            new UrlGu("https://www.kuechengoetter.de/saison-oder-anlass/party-buefett-140").keyword("Party"),
            new UrlGu("https://www.kuechengoetter.de/saison-oder-anlass/picknick-141").keyword("Picknick"),
            new UrlGu("https://www.kuechengoetter.de/saison-oder-anlass/silvester-neujahr-143").keyword("Silvester"),
            new UrlGu("https://www.kuechengoetter.de/saison-oder-anlass/valentinstag-145").keyword("Valentinstag"),
            // Rezepte nach Art der Zubereitung
            new UrlGu("https://www.kuechengoetter.de/art-der-zubereitung/aus-dem-backofen-6").keyword("Backen"),
            new UrlGu("https://www.kuechengoetter.de/art-der-zubereitung/aus-dem-brotbackautomat-7").keyword("Brot"),
            new UrlGu("https://www.kuechengoetter.de/art-der-zubereitung/aus-dem-roemertopf-8").keyword("Römertopf"),
            new UrlGu("https://www.kuechengoetter.de/art-der-zubereitung/aus-der-mikrowelle-9").keyword("Mikrowelle"),
            new UrlGu("https://www.kuechengoetter.de/art-der-zubereitung/aus-der-pfanne-10").keyword("Pfannengerichte"),
            new UrlGu("https://www.kuechengoetter.de/art-der-zubereitung/backen-11").keyword("Backen"),
            new UrlGu("https://www.kuechengoetter.de/art-der-zubereitung/braten-12").keyword("Braten"),
            new UrlGu("https://www.kuechengoetter.de/art-der-zubereitung/dampfgaren-13").keyword("Dampfgaren"),
            new UrlGu("https://www.kuechengoetter.de/art-der-zubereitung/daempfen-14").keyword("Dämpfen"),
            new UrlGu("https://www.kuechengoetter.de/art-der-zubereitung/duensten-15").keyword("Dünsten"),
            new UrlGu("https://www.kuechengoetter.de/art-der-zubereitung/frittieren-16").keyword("Frittieren"),
            new UrlGu("https://www.kuechengoetter.de/art-der-zubereitung/fuer-den-thermomix-248").keyword("Thermomix"),
            new UrlGu("https://www.kuechengoetter.de/art-der-zubereitung/grillen-17").keyword("Grillen"),
            new UrlGu("https://www.kuechengoetter.de/art-der-zubereitung/kochen-18").keyword("Kochen"),
            new UrlGu("https://www.kuechengoetter.de/art-der-zubereitung/niedrigtemperaturgaren-19").keyword("Niedrigtemperaturgaren"),
            new UrlGu("https://www.kuechengoetter.de/art-der-zubereitung/schmoren-20").keyword("Schmoren"),
            new UrlGu("https://www.kuechengoetter.de/art-der-zubereitung/tajine-247").keyword("Tajine"),
            new UrlGu("https://www.kuechengoetter.de/art-der-zubereitung/wokken-21").keyword("Wokken"),
            // Menüfolge - Rezepte für jeden Gang
            new UrlGu("https://www.kuechengoetter.de/menuefolge/beilage-67").keyword("Beilage"),
            new UrlGu("https://www.kuechengoetter.de/menuefolge/brunch-fruehstueck-68").keyword("Brunch"),
            new UrlGu("https://www.kuechengoetter.de/menuefolge/cocktails-drinks-215").keyword("Drinks"),
            new UrlGu("https://www.kuechengoetter.de/menuefolge/dessert-69").keyword("Dessert"),
            new UrlGu("https://www.kuechengoetter.de/menuefolge/hauptspeise-70").keyword("Hauptspeise"),
            new UrlGu("https://www.kuechengoetter.de/menuefolge/haeppchen-fingerfood-71").keyword("Fingerfood"),
            new UrlGu("https://www.kuechengoetter.de/menuefolge/vorspeise-74").keyword("Vorspeise"),
            new UrlGu("https://www.kuechengoetter.de/menuefolge/snacks-72").keyword("Snack"),
            new UrlGu("https://www.kuechengoetter.de/menuefolge/suesse-hauptspeise-73").keyword("Süßspeise")
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
                       result.put(v, new UrlGu(v).keyword(url.getKeyword()));
                    } else {
                        u.keyword(url.getKeyword());
                    }
                }

                for (currentPage = 2; currentPage < countOfPages + 1; currentPage++) {
                    filter = getRecipes(url.getUrl(), currentPage);
                    for (String v : filter.getUrls()) {
                        UrlGu u = result.get(v);
                        if (u == null) {
                           result.put(v, new UrlGu(v).keyword(url.getKeyword()));
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
