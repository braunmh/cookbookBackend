package org.braun.cookbook.backend.model;

import java.util.EnumMap;
import org.braun.cookbook.backend.crawler.ArdBrCrawler;
import org.braun.cookbook.backend.crawler.ArdErnaehrungsdocsCrawler;
import org.braun.cookbook.backend.crawler.ArdHrCrawler;
import org.braun.cookbook.backend.crawler.ArdHrDolceVitaCrawler;
import org.braun.cookbook.backend.crawler.ArdNdrCrawler;
import org.braun.cookbook.backend.crawler.ArdSwrCrawler;
import org.braun.cookbook.backend.crawler.EffileeCrawler;
import org.braun.cookbook.backend.crawler.EssenUndTrinkenCrawler;
import org.braun.cookbook.backend.crawler.BrigitteCrawler;
import org.braun.cookbook.backend.crawler.LeckerCrawler;
import org.braun.cookbook.backend.crawler.WdrEinfachKoestlichCrawler;
import org.braun.cookbook.backend.crawler.WdrHierUndHeuteCrawler;
import org.braun.cookbook.backend.process.BackgroundTask;
import org.braun.cookbook.backend.process.IndexingFacade;

/**
 *
 * @author mbraun
 */
public class BackgroundJobTypeFactory {
    
    private static final BackgroundJobTypeFactory INSTANCE = new BackgroundJobTypeFactory();

    private BackgroundJobTypeFactory() {
        JOB_DEFINITIONS.put(BackgroundJobType.Indexer, IndexingFacade.class);
        JOB_DEFINITIONS.put(BackgroundJobType.ArdErnaehrungsdocsCrawler, ArdErnaehrungsdocsCrawler.class);
        JOB_DEFINITIONS.put(BackgroundJobType.ArdNdrCrawler, ArdNdrCrawler.class);
        JOB_DEFINITIONS.put(BackgroundJobType.ArdSwrCrawler, ArdSwrCrawler.class);
        JOB_DEFINITIONS.put(BackgroundJobType.EssenUndTrinkenCrawler, EssenUndTrinkenCrawler.class);
        JOB_DEFINITIONS.put(BackgroundJobType.EffileeCrawler, EffileeCrawler.class);
        JOB_DEFINITIONS.put(BackgroundJobType.BrigitteCrawler, BrigitteCrawler.class);
        JOB_DEFINITIONS.put(BackgroundJobType.LeckerCrawler, LeckerCrawler.class);
        JOB_DEFINITIONS.put(BackgroundJobType.WdrEinfachKoestlichCrawler, WdrEinfachKoestlichCrawler.class);
        JOB_DEFINITIONS.put(BackgroundJobType.WdrHierUndHeuteCrawler, WdrHierUndHeuteCrawler.class);
        JOB_DEFINITIONS.put(BackgroundJobType.ArdHrCrawler, ArdHrCrawler.class);
        JOB_DEFINITIONS.put(BackgroundJobType.ArdHrDolceVitaCrawler, ArdHrDolceVitaCrawler.class);
        JOB_DEFINITIONS.put(BackgroundJobType.ArdBrCrawler, ArdBrCrawler.class);
    }
    
    private final EnumMap<BackgroundJobType, Class<? extends BackgroundTask>> JOB_DEFINITIONS = new EnumMap<>(BackgroundJobType.class);
    
    public static Class<? extends BackgroundTask> getBackgroudJobClass(BackgroundJobType type) {
        return INSTANCE.JOB_DEFINITIONS.get(type);
    } 
}
