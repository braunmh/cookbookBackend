package org.braun.cookbook.backend.process;

import jakarta.ejb.AsyncResult;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Named;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.concurrent.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.braun.cookbook.backend.model.Recipe;
import org.braun.cookbook.backend.model.RecipeSolr;
import org.braun.cookbook.util.Configuration;
import org.xml.sax.SAXException;

/**
 *
 * @author mbraun
 */
@Named
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class IndexingFacade {

    private String solrUrl;

    private String solrCollection;
    
    private String contentDirectory;

    private static final Logger LOG = LogManager.getLogger();

    public String getSolrUrl() {
        if (solrUrl == null) {
            solrUrl = Configuration.getInstance().getSolrUrl();
        }
        return solrUrl;
    }

    public String getSolrCollection() {
        if (solrCollection == null) {
            solrCollection = Configuration.getInstance().getSolrCollection();
        }
        return solrCollection;
    }

    public String getContentDirectory() {
        if (contentDirectory == null) {
            contentDirectory = Configuration.getInstance().getContentDirectory();
        }
        return contentDirectory;
    }

    @Asynchronous
    public Future<Integer> index() {
        return new AsyncResult<>(indexIntern());
    }
    
    public Integer indexIntern() {
        int indexed = 0;
        if (!StatusFactory.getInstance().aquireIndexStatusBussy()) {
            return indexed;
        }
        try (
            DirectoryComparer dc = new DirectoryComparer(getContentDirectory(), new XmlFilter());
            SolrClient client = getSolrClient()) {
            while (dc.hasNext()) {
                DirectoryComparer.Entry entry = dc.next();
                switch (entry.getOperation()) {
                    case remove -> {
                        client.deleteById(getSolrCollection(), getId(entry.getName()));
                    }
                    case merge, persist -> {
                        RecipeSolr rs = getRecipe(entry.getName());
                        if (rs != null) {
                            client.addBean(getSolrCollection(), rs);
                        }
                    }
                }
                if (indexed++ % 100 == 0) {
                    client.commit(getSolrCollection());
                    LOG.info("Number of entries indexed: {}", indexed);
                }
            }
        } catch (IOException | SolrServerException e) {
            LOG.error("Indexing finished with error.", e);
        } catch (Exception e) {
            LOG.error("Fatal error", e);
        }
        StatusFactory.getInstance().aquireStatusStatusDone();
        return indexed;
    }

    public String getId(String relativeName) {
        int beginId = relativeName.lastIndexOf('/');
        int endId = relativeName.lastIndexOf('.');
        return relativeName.substring(beginId + 1, endId);
    }
    
    private RecipeSolr getRecipe(String relativeName) {
        File file = new File(getContentDirectory() + "/" + relativeName);
        if (file.exists()) {
            try {
                Recipe recipe = Recipe.unmarshal(getContentDirectory(), relativeName);
                recipe.setModified(file.lastModified());
                return new RecipeSolr(recipe);
            } catch (SAXException e) {
                LOG.error("unmarshal Recipe: {}", file.getPath());
                return null;
            }
        } else {
            return null;
        }
    }
    
    private SolrClient getSolrClient() {
        return new Http2SolrClient.Builder(getSolrUrl()).build();
    }

    public static class XmlFilter implements FileFilter {

        @Override
        public boolean accept(File pathname) {
            return pathname.isDirectory() || pathname.getName().endsWith(".xml");
        }

    }

}
