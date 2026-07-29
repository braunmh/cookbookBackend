package org.braun.cookbook.backend.crawler.gu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.braun.cookbook.backend.crawler.CrawlerTest;
import org.braun.cookbook.backend.model.Keyword;
import org.braun.cookbook.backend.model.Recipe;
import org.braun.cookbook.backend.model.recipe.Category;
import org.braun.cookbook.backend.process.KeywordFactory;
import org.braun.cookbook.backend.process.RecipeFacade;
import org.junit.jupiter.api.Test;

/**
 *
 * @author mbraun
 */
public class GuCrawlerTest extends CrawlerTest {

//    @Test
    public void guGetNewRecipesTest() throws IOException {
        GuCrawler crawler = new GuCrawler();
        List<UrlGu> res = crawler.getNewRecipes();
        for (UrlGu u : res) {
            System.out.println(u.getUrl() + ", " + u.getKeyword());
        }
        Recipe recipe = crawler.getRecipe(res.getLast());
        if (recipe != null) {
            StringWriter writer = new StringWriter();
            recipe.marshall(writer);
            System.out.println(writer.toString());
        }
    }

//    @Test
    public void testKeywords() {
        GuCrawler crawler = new GuCrawler();
        initT();
        for (UrlGu u : crawler.sites) {
            for (String k : u.getKeyword()) {
                Keyword ki = KeywordFactory.getInstance().getByName(k);
                if (ki == null) {
                    System.out.println(u.getUrl());
                    System.out.println("\t keyword not found " + k);
                }
            }
        }
    }

    @Test
    public void testGetRecipes() {
        initT();
        GuCrawler crawler = new GuCrawler();
        RecipeFacade recipeFacade = getRecipeFacade();
        crawler.setRecipeFacade(recipeFacade);
        Set<String> unknownKeywords = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("/opt/solr/data/cookbook/gu/newsLast.txt"))) {
            String line = reader.readLine();

            int countRead = 0;
            int countInsert = 0;
            while (null != (line = reader.readLine())) {
                String[] parts = line.split("\t");
                UrlGu url = new UrlGu().url(parts[0]).count(countRead);
                
                for (int i = 1; i < parts.length; i++) {
                    url.keyword(parts[i]);
                }
                if (recipeFacade.findByUrl(url.toString()) != null) {
                    continue;
                }
                Recipe recipe = crawler.getRecipe(url);
                if (recipe != null) {
                    String pathParent = crawler.getPathParent(recipe, url);
                    if (recipe.getSource().isEmpty()) {
                        recipe.getSource().setValue(pathParent);
                    }
                    recipe.getSource().setUrl(url.toString());
                    Set<Category> converted = new HashSet<>(recipe.getCategories().getCategories().size());
                    for (Category c : recipe.getCategories().getCategories()) {
                        Keyword k = KeywordFactory.getInstance().getByName(c.getName());
                        if (k != null) {
                            converted.add(new Category().name(String.valueOf(k.getId())));
                        } else {
                            unknownKeywords.add(c.getName().toUpperCase());
                        }
                    }
                    recipe.getCategories().getCategories().clear();
                    recipe.getCategories().getCategories().addAll(converted);
                    byte[] image = crawler.getImage(recipe.getImageUrl());
                    recipe.setId(null);
                    recipeFacade.insert(recipe, pathParent, image);
                    countInsert++;
                }
                if (countInsert % 100 == 0) {
                    System.out.println(countInsert);
                }
                countRead++;
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        System.out.println(unknownKeywords);
    }
}
