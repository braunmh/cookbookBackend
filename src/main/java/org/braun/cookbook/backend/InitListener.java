package org.braun.cookbook.backend;

import jakarta.inject.Inject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.braun.cookbook.backend.process.KeywordFacade;
import org.braun.cookbook.backend.process.KeywordFactory;
import org.braun.cookbook.util.Configuration;

/**
 *
 * @author mbraun
 */
@WebListener
public class InitListener implements ServletContextListener {
    
    private static final Logger LOG = LogManager.getLogger();

    @Inject
    private KeywordFacade KeywordFacade;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOG.info("Initialize Appliction");
        try {
            initConfiguartion();
        } catch (IOException e) {
            LOG.fatal("Initialation failed. Can not read configuartion-file.", e);
        }
        KeywordFactory.getInstance().refresh(KeywordFacade.findAll());
        LOG.info("Initialize Appliction done.");
    }
    
    private void initConfiguartion()  throws IOException {
        String configFile = (System.getProperty("org.braun.cookbook.config"));
        
        InputStream inputStream = (configFile == null) 
                ? this.getClass().getClassLoader().getResourceAsStream("/config.xml")
                : new FileInputStream(configFile);
        Configuration.init(inputStream);
    }
}
