package org.braun.cookbook.backend.crawler;

import org.braun.cookbook.backend.process.BaseTest;
import org.braun.cookbook.backend.process.KeywordFactory;
import org.junit.jupiter.api.Test;

/**
 *
 * @author mbraun
 */
public class ArdNdrTest extends BaseTest {

    @Test
    public void ardNdrTest() {
        init();
        KeywordFactory.getInstance().refresh(getKeywordFacade().findAll());
        ArdNdrCrawler crawler = new ArdNdrCrawler();
        crawler.setRecipeFacade(getRecipeFacade());
        try {
            crawler.execute();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
    @Test
    public void ardSwrTest() {
        init();
        KeywordFactory.getInstance().refresh(getKeywordFacade().findAll());
        ArdSwrCrawler crawler = new ArdSwrCrawler();
        crawler.setRecipeFacade(getRecipeFacade());
        try {
            crawler.execute();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
