package org.braun.cookbook.backend.importer;

import java.io.FileInputStream;
import java.io.InputStream;
import org.braun.cookbook.backend.process.BaseTest;
import org.braun.cookbook.backend.process.KeywordFactory;
import org.junit.jupiter.api.Test;

/**
 *
 * @author mbraun
 */
public class OOImporterTest extends BaseTest {
    
    @Test
    public void test() {
        String ooName = "/home/mbraun/Dokumente/recipes/ernaehrungsDocs.odt";
        try (InputStream inputStream = new FileInputStream(ooName)) {
            init();
            KeywordFactory.getInstance().refresh(getKeywordFacade().findAll());
            getEntityManager().getTransaction().begin();
            OpenOfficeImporter imp = new OpenOfficeImporter(getRecipeFacade(), inputStream, "ernaehrungsDocs");
            imp.importRecipes();
            getEntityManager().getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
    
}
