package org.braun.cookbook.backend.process;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.spi.PersistenceUnitTransactionType;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.braun.cookbook.backend.dao.JobDao;
import org.braun.cookbook.backend.dao.KeywordDao;
import org.braun.cookbook.backend.dao.SequenceDao;
import org.braun.cookbook.util.Configuration;
import org.eclipse.persistence.config.PersistenceUnitProperties;

/**
 *
 * @author mbraun
 */
public class BaseTest {
    Logger LOG = LogManager.getLogger();

    protected String solrCollection = "cookbook";

    private EntityManager em;

    public BaseTest() {
    }

    protected void init() {
        try {
            Configuration.init(new FileInputStream("/data/develop/cookbook/src/main/resources/config.xml"));
        } catch (IOException e) {
            System.out.println("Initialisierung fehlgeschlagen");
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        }
    }
    
    protected SolrClient getSolrClient() {
        final String solrUrl = "http://localhost:8983/solr";
        return new Http2SolrClient.Builder(solrUrl).build();
    }

    protected EntityManager getEntityManager() {
        if (em == null) {
            try {
                final Map<String, Object> props = new HashMap<>();
                props.put(PersistenceUnitProperties.TRANSACTION_TYPE, PersistenceUnitTransactionType.RESOURCE_LOCAL.name());
                props.put(PersistenceUnitProperties.JDBC_DRIVER, "com.mysql.cj.jdbc.Driver");
                props.put(PersistenceUnitProperties.JDBC_URL, "jdbc:mysql://192.168.0.219:3306/cookbook?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC");
                props.put(PersistenceUnitProperties.JDBC_USER, "cookbook");
                props.put(PersistenceUnitProperties.JDBC_PASSWORD, "gesa0403");
                props.put("eclipselink.id-validation", "NULL");
                EntityManagerFactory emf = Persistence.createEntityManagerFactory("cookbook", props);
                em = emf.createEntityManager();
                return em;

            } catch (Exception e) {
                LOG.error(e.getMessage());
                return null;
            }
        }
        return em;
    }
    
    private JobFacade jobFacade;
    public JobFacade getJobFacade() {
        if (jobFacade == null) {
            jobFacade = new JobFacade();
            jobFacade.setJobDao(getJobDao());
        }
        return jobFacade;
    }
    
    private JobDao jobDao;
    private JobDao getJobDao() {
        if (jobDao == null) {
            jobDao = new JobDao(getEntityManager());
        }
        return jobDao;
    }
    
    private IndexingFacade houseKeepingFacade;

    public IndexingFacade getHouseKeepingFacade() {
        if (houseKeepingFacade == null) {
            houseKeepingFacade = new IndexingFacade();
        }
        return houseKeepingFacade;
    }
    
    private KeywordFacade keywordFacade;
    
    public KeywordFacade getKeywordFacade() {
        if (keywordFacade == null) {
            keywordFacade = new KeywordFacade();
            keywordFacade.setKeywordDao(getKeywordDao());
        }
        return keywordFacade;
    }
    
    private KeywordDao keywordDao;
    public KeywordDao getKeywordDao() {
        if (keywordDao == null) {
            keywordDao = new KeywordDao(getEntityManager());
        } 
        return keywordDao;
    }
    
    private SequenceGenerator sequenceGenerator;
    protected SequenceGenerator getSequenceGenerator() {
        if (sequenceGenerator == null) {
            sequenceGenerator = new SequenceGenerator();
            sequenceGenerator.setSequenceDao(getSequenceDao());
        }
        return sequenceGenerator;
    }
    
    private SequenceDao sequenceDao;
    protected SequenceDao getSequenceDao() {
        if (sequenceDao == null) {
            sequenceDao = new SequenceDao(getEntityManager());
        }
        return sequenceDao;
    }
    
    private RecipeFacade recipeFacade;

    public RecipeFacade getRecipeFacade() {
        if (recipeFacade == null) {
            recipeFacade = new RecipeFacade();
            recipeFacade.setSequenceGenerator(getSequenceGenerator());
        }
        return recipeFacade;
    }
    
    
}
