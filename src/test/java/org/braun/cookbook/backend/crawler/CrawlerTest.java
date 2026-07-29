package org.braun.cookbook.backend.crawler;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.braun.cookbook.backend.model.BackgroundJobType;
import org.braun.cookbook.backend.model.JobResult;
import org.braun.cookbook.backend.model.JobStatus;
import org.braun.cookbook.backend.model.Recipe;
import org.braun.cookbook.backend.model.recipe.Category;
import org.braun.cookbook.backend.process.BaseTest;
import org.braun.cookbook.backend.process.KeywordFactory;
import org.junit.jupiter.api.Test;

/**
 *
 * @author mbraun
 */
public class CrawlerTest extends BaseTest {

    protected void initT() {
        init();
        KeywordFactory.getInstance().refresh(getKeywordFacade().findAll());
    }
    
    @Test
    public void brigitteRecipesTest() throws IOException {
        initT();
        BrigitteCrawler crawler = new BrigitteCrawler();
        Set<String> unkownKeywords = new HashSet<>();
        for (String url : crawler.getNewRecipes()) {
            Recipe recipe = crawler.getRecipe(url);
            System.out.println((recipe == null) ? "false " : "true  " + url);
            if (recipe != null) {
                for (Category c : recipe.getCategories().getCategories()) {
                    var keyword = KeywordFactory.getInstance().getByName(c.getName());
                    if (keyword == null) {
                        unkownKeywords.add(c.getName());
                        System.out.println("unknown " + c.getName());
                    } else {
                        System.out.println("known   " + keyword.getName());
                    }
                }
                StringWriter sw = new StringWriter();
                recipe.marshall(sw);
                System.out.println(sw.toString());
                //break;
            }
        }
        List<String> l = unkownKeywords.stream().sorted().toList();
        for (var k : l) {
            System.out.println(k);
        }
    }
    
    @Test
    public void ardHrGetRecipesTest() {
        ArdHrCrawler crawler = new ArdHrCrawler();
        for (String url : crawler.getNewRecipes()) {
            System.out.println(url);
        }
    }
    
    @Test
    public void ardHrGetRecipeTest() throws IOException {
        initT();
        List<String> urls = List.of("https://www.hr-fernsehen.de/sendungen-a-z/hallo-hessen/rezepte/rezept-penne-mit-basilikum-petersilien-schmand-und-gebratenen-waldpilzen-v1,kochen-11264.html",
        "https://www.hr-fernsehen.de/sendungen-a-z/hallo-hessen/rezepte/rezept-hausgebeizter-lachs-mit-honig-senf-sauce-und-spargel-in-der-folie-v1,kochen-11232.html"
        );
        ArdHrCrawler crawler = new ArdHrCrawler();
        for (String url : urls) {
            Recipe recipe = crawler.getRecipe(url);
            if (recipe != null) {
                StringWriter writer = new StringWriter();
                recipe.marshall(writer);
                System.out.println(writer.toString());
            }
        }
    }
    
    @Test
    public void ardHrDolceVitaGetRecipesTest() {
        ArdHrDolceVitaCrawler crawler = new ArdHrDolceVitaCrawler();
        for (String url : crawler.getNewRecipes()) {
            System.out.println(url);
        }
    }

    @Test
    public void ardEffileeGetRecipesTest() {
        EffileeCrawler crawler = new EffileeCrawler();
        for (String url : crawler.getNewRecipes()) {
            System.out.println(url);
        }
    }
    
    
    @Test
    public void ardBrGetRecipesTest() {
        ArdBrCrawler crawler = new ArdBrCrawler();
        for (String url : crawler.getNewRecipes()) {
            System.out.println(url);
        }
    }
    
    @Test
    public void ardBrGetRecipeTest() throws IOException {
        initT();
        List<String> urls = List.of(
            "https://www.br.de/br-fernsehen/sendungen/wir-in-bayern/rezepte/frikadelle-kraeuter-polenta-pflanzerl-fenchel-erdbeer-tomaten-salsa-wolfgang-link-100.html",
            "https://www.br.de/br-fernsehen/sendungen/wir-in-bayern/rezepte/kuchen-erdbeerkuchen-zitronenmelisse-martina-harrecker-100.html"
        );
        ArdBrCrawler crawler = new ArdBrCrawler();
        for (String url : urls) {
            Recipe recipe = crawler.getRecipe(url);
            if (recipe != null) {
                StringWriter writer = new StringWriter();
                recipe.marshall(writer);
                System.out.println(writer.toString());
            }
        }
    }
    
    @Test
    public void effileeGetRecipeTest() throws IOException {
        initT();
        List<String> urls = List.of(
            "https://www.spiegel.de/effilee/rezept/koenigsberger-spaghetti-a-0e939e85-0005-0011-0000-000010154642",
"https://www.spiegel.de/effilee/rezept/avocado-mit-ei-auf-kreolischem-reis-a-3dd50986-0005-0011-0000-000010132245",
"https://www.spiegel.de/effilee/rezept/galette-occitane-mit-eingelegten-weintrauben-mandeln-pinienkernen-a-c4ec9bbb-0005-0011-0000-000000005916"
        );
        EffileeCrawler crawler = new EffileeCrawler();
        for (String url : urls) {
            Recipe recipe = crawler.getRecipe(url);
            if (recipe != null) {
                StringWriter writer = new StringWriter();
                recipe.marshall(writer);
                System.out.println(writer.toString());
            }
        }
    }
    
    @Test
    public void ardHrDolceVitaGetRecipeTest() throws IOException {
        initT();
        List<String> urls = List.of("https://www.hr1.de/sendungen/dolce-vita/rezept-fuer-pomelosalat-mit-garnelen-und-erdnuessen-v1,pomelosalat-102.html"
        );
        ArdHrDolceVitaCrawler crawler = new ArdHrDolceVitaCrawler();
        for (String url : urls) {
            Recipe recipe = crawler.getRecipe(url);
            if (recipe != null) {
                StringWriter writer = new StringWriter();
                recipe.marshall(writer);
                System.out.println(writer.toString());
            }
        }
    }
    
    @Test
    public void wdrEinfachKoestlichGetRecipesTest() {
        WdrEinfachKoestlichCrawler crawler = new WdrEinfachKoestlichCrawler();
        for (String url : crawler.getNewRecipes()) {
            System.out.println(url);
        }
    }
    
    @Test
    public void wdrEinfachKoestlichGetRecipeTest() {
        String [] urls = new String[] {
            "https://www1.wdr.de/verbraucher/rezepte/euk-sueden-fenchel-orangen-salat-100.html",
            "https://www1.wdr.de/verbraucher/rezepte/euk-freitag-klipp-koenigsberger-klopse-106.html",
            "https://www1.wdr.de/verbraucher/rezepte/tomaten-tonnato-100.html"
        };
        WdrEinfachKoestlichCrawler crawler = new WdrEinfachKoestlichCrawler();
        for (String url : urls) {
            try {
                Recipe recipe = crawler.getRecipe(url);
                if (recipe != null) {
                    StringWriter writer = new StringWriter();
                    recipe.marshall(writer);
                    System.out.println(writer.toString());
                }
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
       
    }
    
    @Test
    public void ardNdrTest() {
        ArdNdrCrawler crawler = new ArdNdrCrawler();
        try {
            Recipe recipe = crawler.getRecipe("https://www.ndr.de/ratgeber/kochen/rezepte/wuerziges-blumenkohl-curry-mit-kokosmilch-und-duftreis,blumenkohlcurry-100.html");
            StringWriter writer = new StringWriter();
            recipe.marshall(writer);
            System.out.println(writer.toString());
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
    @Test
    public void ardSwrTest() {
        ArdSwrCrawler crawler = new ArdSwrCrawler();
        executeCrawler(crawler, BackgroundJobType.ArdSwrCrawler);
    }
    
    @Test
    public void ernaehrungsdocs() {
        ArdErnaehrungsdocsCrawler crawler = new ArdErnaehrungsdocsCrawler();
        executeCrawler(crawler, BackgroundJobType.ArdErnaehrungsdocsCrawler);
    }
    
    @Test
    public void essenUndTrinken() {
        EssenUndTrinkenCrawler crawler = new EssenUndTrinkenCrawler();
        executeCrawler(crawler, BackgroundJobType.EssenUndTrinkenCrawler);
    }
    
    
    private void executeCrawler(Crawler crawler, BackgroundJobType type) {
        initT();
        crawler.setEntityManager(getEntityManager());
        crawler.setJobFacade(getJobFacade());
        crawler.setRecipeFacade(getRecipeFacade());
        JobResult result;
        try {
            getJobFacade().begin(type);
            result = crawler.doExecute();
            System.out.println(result);
        } catch (Exception e) {
            result = new JobResult().type(type).status(JobStatus.error).message(e.getMessage());
            e.printStackTrace(System.out);
        }
        getJobFacade().end(result);
    }
}
