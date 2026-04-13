package org.braun.cookbook.backend.model;

import java.util.EnumMap;
import org.braun.cookbook.backend.crawler.ArdErnaehrungsdocsCrawler;
import org.braun.cookbook.backend.crawler.ArdNdrCrawler;
import org.braun.cookbook.backend.crawler.ArdSwrCrawler;
import org.braun.cookbook.backend.crawler.EssenUndTrinkenCrawler;
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
    }
    
    private final EnumMap<BackgroundJobType, Class<? extends BackgroundTask>> JOB_DEFINITIONS = new EnumMap<>(BackgroundJobType.class);
    
    public static Class<? extends BackgroundTask> getBackgroudJobClass(BackgroundJobType type) {
        return INSTANCE.JOB_DEFINITIONS.get(type);
    } 
}
