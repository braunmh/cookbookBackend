package org.braun.cookbook.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author mbraun
 */
public class Configuration {
 
    private static Configuration INSTANCE;
    
    private Properties props;
    
    private Configuration() {
        
    }
    
    public static void init(InputStream is) throws IOException {
        INSTANCE = new Configuration();
        INSTANCE.props = new Properties();
        INSTANCE.props.loadFromXML(is);
    }
    
    public static Configuration getInstance() {
        return INSTANCE;
    }
    
    public String getSolrUrl() {
        return props.getProperty("org.braun.cookbook.solr.url");
    }
    
    public String getSolrCollection() {
        return props.getProperty("org.braun.cookbook.solr.collection");
    }
    
    public String getContentDirectory() {
        return props.getProperty("org.braun.cookbook.content.dir");
    }
    
    public String getProperty(String key ) {
        return props.getProperty(key);
    }
}
