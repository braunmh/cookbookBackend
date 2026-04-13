package org.braun.cookbook.backend.crawler;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Named;
import org.braun.cookbook.backend.model.BackgroundJobType;

/**
 *
 * @author mbraun
 */
@Named
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ArdErnaehrungsdocsCrawler extends ArdNdrCrawler {

    @Override
    protected String getPathParent() {
        return "ernaehrungsDocs";
    }

    @Override
    protected String getSite() {
        return "/ratgeber/kochen/rezepte/Rezepte-von-den-Ernaehrungs-Docs,edocsrezepte102.html";
    }

    @Override
    public BackgroundJobType getTaskName() {
        return BackgroundJobType.ArdErnaehrungsdocsCrawler;
    }
    
}
