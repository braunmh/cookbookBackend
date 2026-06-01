package org.braun.cookbook.backend.migration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.braun.cookbook.backend.model.BackgroundJobType;
import org.braun.cookbook.backend.model.JobResult;
import org.braun.cookbook.backend.model.JobStatus;
import org.braun.cookbook.backend.model.Keyword;
import org.braun.cookbook.backend.model.Recipe;
import org.braun.cookbook.backend.model.RecipeShort;
import org.braun.cookbook.backend.model.RecipeSolr;
import org.braun.cookbook.backend.model.Suggestion;
import org.braun.cookbook.backend.model.recipe.Category;
import org.braun.cookbook.backend.process.BaseTest;
import org.braun.cookbook.backend.process.IndexingFacade;
import org.braun.cookbook.backend.process.KeywordFacade;
import org.braun.cookbook.backend.process.KeywordFactory;
import org.braun.cookbook.backend.process.RecipeFacade;
import org.braun.cookbook.backend.process.SequenceGenerator;
import org.braun.cookbook.common.CookBookException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

/**
 *
 * @author mbraun
 */
public class MigrationRecipe extends BaseTest {
    
    private final String sourceDirectory = "/data/CookBookJSF";
    private final String targetDirectory = "/opt/solr/data/cookbook/content";
    private Properties props;
    
    @Test
    public void replaceKeyword() {
        init();
        KeywordFactory.getInstance().refresh(getKeywordFacade().findAll());
        Keyword keywordOld = KeywordFactory.getInstance().getById(76l);
        Keyword keywordNew = KeywordFactory.getInstance().getById(143l);
        RecipeFacade recipeFacade = getRecipeFacade();
        List<Long> keywords = List.of(keywordOld.getId());
        try {
            List<RecipeShort> res = recipeFacade.searchByAttributes(null, keywords, null, null, Boolean.FALSE, null, null);
            for (RecipeShort rs : res) {
                Recipe r = Recipe.unmarshal(targetDirectory, rs.getPath());
                Set<Category> cats = new HashSet<>(r.getCategories().getCategories());
                cats.removeIf(c -> c.getName().equals(String.valueOf(keywordOld.getId())));
                cats.add(new Category().name(String.valueOf(keywordNew.getId())));
                r.getCategories().getCategories().clear();
                r.getCategories().getCategories().addAll(cats);
                r.marshall(targetDirectory, rs.getPath());
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
    
    // @Test
    public void migrateImages() {
        String directoryName = targetDirectory + "/DasKochrezept";
        File directory = new File(directoryName);
        analyzeRecipes(directory);
    }
    
    private void analyzeRecipes(File directory) {
        String relativePath = directory.getPath().substring(targetDirectory.length() + 1);
        System.out.println(relativePath);
        for (File entry : directory.listFiles((File dir, String name) -> (dir.isFile() && (name.endsWith(".xml"))) || dir.isDirectory())) {
            if (entry.isFile()) {
                try {
                    Recipe recipe = Recipe.unmarshal(entry.getParent(), entry.getName());  
                    if (recipe.getImageUrl() != null && recipe.getImageUrl().startsWith("Recipes/")) {
                        String newImageUrl = relativePath + "/" + recipe.getId() + ".jpg";
                        if (copyImage(recipe.getImageUrl(), newImageUrl)) {
                            recipe.setImageUrl(newImageUrl);
                            recipe.marshall(entry.getParent(), entry.getName());
                        }
                    }
                } catch (SAXException | IOException e) {
                    if (entry.getName().endsWith(".xml")) {
                        System.out.println(String.format("Can not unmarshal recipe %s", entry.getPath()));
                    }
                }
            } else {
                analyzeRecipes(entry);
            }
        }
    }
    
    private boolean copyImage(String sourceUrl, String targetUrl) {
        System.out.println(String.format("source = %s, target = %s", sourceUrl, targetUrl));
        try (FileInputStream inputStream = new FileInputStream(new File(sourceDirectory + "/" + sourceUrl));
             FileOutputStream outputStream = new FileOutputStream(new File(targetDirectory + "/" + targetUrl))) {
            outputStream.write(inputStream.readAllBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace(System.out);
            return false;
        }
    }
    
    public void toRecipeSolr() {
        File entry = new File(sourceDirectory + "/Kueche/Thailand/0015.xml");
        try {
            Recipe recipe = Recipe.unmarshal(entry.getParent(), entry.getName());            
            Path p = Paths.get(entry.getPath());
            FileTime ct = (FileTime) Files.getAttribute(p, "creationTime");
            if (ct != null) {
                recipe.setCreated(ct.toMillis());
            }
            recipe.setModified(entry.lastModified());
            RecipeSolr rs = new RecipeSolr(recipe);
            System.out.println(rs.getContent());
            recipe.marshall(System.out);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        System.out.println("Süßspeise".toUpperCase());
    }
    
    public void pathParent() {
        String path = "ARD/ndr/2026/12345.xml";
        int i = path.lastIndexOf('/');
        String pathParent = (i > 0) ? path.substring(0, i) : "";
        System.out.println(pathParent);
    }
    public void suggest() {
        init();
        String[] values = new String[] {"s\u0276u", "Näh", "fraî", "größ", "groess", "Crème"};
        for (String value : values) {
            System.out.println("Suggestions for " + value);
            RecipeFacade recipeFacade = getRecipeFacade();
            List<Suggestion> result = recipeFacade.getSuggestion("contentSuggest", value);
            for (Suggestion r : result) {
                System.out.println("  " + r.getName() + " (" + r.getFrequency() + ")");
            }
        }
    }

    // @Test
    public void indexing() {
        init();
        IndexingFacade houseKeepingFacade = getHouseKeepingFacade();
        houseKeepingFacade.setEntityManager(getEntityManager());
        houseKeepingFacade.setJobFacade(getJobFacade());
        JobResult result;
        try {
            getJobFacade().begin(BackgroundJobType.Indexer);
            result = houseKeepingFacade.doExecute();
        } catch (CookBookException e) {
            result = new JobResult().status(JobStatus.error).type(BackgroundJobType.Indexer).message(e.getMessage());
        }
        getJobFacade().end(result);
        System.out.println(result.getMessage());
    }
    
    public void migrateRecipes() {
        props = new Properties();
        try {
            props.loadFromXML(new FileInputStream("/data/CookBookJSF/WEB-INF/conf/country.xml"));
        } catch (IOException e) {
            e.printStackTrace(System.out);
            return;
        }
        KeywordFactory.getInstance().refresh(getKeywordFacade().findAll());
        getEntityManager().getTransaction().begin();
        migrate(new File(sourceDirectory));
        getEntityManager().getTransaction().commit();
    }
    
    int migrated = 0;
    List<String> toSkip = List.of("3lands", "edeka", "lafer", "lingen", "news", "rt", "tg", "mbraun", "umschau", "ZDF");
    private void migrate(File directory) {
        SequenceGenerator sg = getSequenceGenerator();
        for (File entry : directory.listFiles()) {
            if (entry.isDirectory()) {
//                if (toSkip.contains(entry.getName())) {
//                    continue;
//                }
                File dc = new File(this.targetDirectory + entry.getPath().substring(sourceDirectory.length()));
                if (!dc.exists()) {
                    dc.mkdirs();
                }
                System.out.println("migrate " + entry.getPath());
                migrate(entry);
            } else if (entry.getName().endsWith(".xml")) {
                try {
                    if (sourceDirectory.length() + 1 >= entry.getParent().length()) {
                        System.out.println("Possible index out of bounds " + entry.getParent());
                        continue;
                    }
                    String relativePath = entry.getParent().substring(sourceDirectory.length() + 1);
                    String target = this.targetDirectory + "/" + relativePath;
                    Recipe recipe = Recipe.unmarshal(entry.getParent(), entry.getName());
                    Path p = Paths.get(entry.getPath());
                    FileTime ct = (FileTime) Files.getAttribute(p, "creationTime");
                    if (ct != null) {
                        recipe.setCreated(ct.toMillis());
                    }
                    recipe.setModified(entry.lastModified());
                    recipe.setId(String.valueOf(sg.getNext()));
                    List<Category> newc = new ArrayList<>();
                    if (recipe.getCountry() != null) {
                        String country = props.getProperty(recipe.getCountry());
                        if (country != null) {
                            recipe.getCategories().add(new Category().name(country));
                        }
                    }
                    for (Category category : recipe.getCategories().getCategories()) {
                        Keyword k = KeywordFactory.getInstance().getByName(category.getName());
                        if (k == null) {
                            String x = String.format("%s/%s %s not found", entry.getParent(), entry.getName(), category.getName());
                            System.out.println(x);
                        } else {
                            newc.add(new Category().name(String.valueOf(k.getId())));
                        }
                    }
                    recipe.getCategories().getCategories().clear();
                    recipe.getCategories().getCategories().addAll(newc);
                    recipe.setRelativeName(relativePath + "/" + recipe.getId() + ".xml");

                    if (recipe.getImageUrl() != null && !recipe.getImageUrl().isBlank()) {
                        File pictureFile = new File(sourceDirectory + "/" + recipe.getImageUrl());
                        if (!pictureFile.exists()) {
                            pictureFile = new File(entry.getParent() + "/" + recipe.getImageUrl());
                        }
                        int beginExtension = recipe.getImageUrl().lastIndexOf('.');
                        if (pictureFile.exists() && beginExtension > 0) {
                            String urlPicture = relativePath + "/" + recipe.getId() + recipe.getImageUrl().substring(beginExtension);
                            recipe.setImageUrl(urlPicture);
                            try (InputStream is = new FileInputStream(pictureFile);
                                OutputStream os = new FileOutputStream(new File(targetDirectory + "/" + recipe.getImageUrl()));) {
                                byte[] buffer = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = is.read(buffer)) > -1) {
                                    os.write(buffer, 0, bytesRead);
                                }
                            }
                        }
                    }
                    recipe.marshall(target, recipe.getRelativeName());
                } catch (IOException | SAXException e) {
                    e.printStackTrace(System.out);
                }
            }
            if (migrated++ % 100 == 0) {
                getEntityManager().getTransaction().commit();
                getEntityManager().getTransaction().begin();
            }
        }
    }
    
   // // @Test
    public void migrateCountries() {
        KeywordFacade keywordFacade = getKeywordFacade();
        props = new Properties();
        try {
            props.loadFromXML(new FileInputStream("/data/CookBookJSF/WEB-INF/conf/country.xml"));
        } catch (IOException e) {
            e.printStackTrace(System.out);
            return;
        }
        getEntityManager().getTransaction().begin();
        Keyword root = keywordFacade.getByName("Root");
        String lastInsert = null;
        for (Object key : props.keySet()) {
            String value = (String) props.get(key);
            if (keywordFacade.getByName(value) == null) {
                lastInsert = value;
                System.out.println("Add Country " + value);
                Keyword k = new Keyword()
                        .name(value)
                        .nameUpper(value.toUpperCase())
                        .parentId(root.getId());
                keywordFacade.insert(k);
            }
        }
        getEntityManager().getTransaction().commit();
        if (lastInsert != null) {
            System.out.println(keywordFacade.getByName(lastInsert).getId());
        }
    }
}
