package org.braun.cookbook.backend.crawler;

import org.braun.cookbook.backend.model.BackgroundJobType;
import org.braun.cookbook.backend.model.Job;
import org.braun.cookbook.backend.model.JobResult;
import org.braun.cookbook.backend.model.JobStatus;
import org.braun.cookbook.backend.model.Recipe;
import org.braun.cookbook.backend.process.BaseTest;
import org.braun.cookbook.backend.process.KeywordFactory;
import org.junit.jupiter.api.Test;

/**
 *
 * @author mbraun
 */
public class CrawlerTest extends BaseTest {

    private void initT() {
        init();
        KeywordFactory.getInstance().refresh(getKeywordFacade().findAll());
    }
    
    @Test
    public void ardNdrTest() {
        ArdNdrCrawler crawler = new ArdNdrCrawler();
        executeCrawler(crawler, BackgroundJobType.ArdNdrCrawler);
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
