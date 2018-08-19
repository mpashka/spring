package com.github.mpashka.spring.config;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

/**
 *
 */
public class ReloadableResourcePropertySource extends PropertySource<EncodedResource> {

    private static final Log log = LogFactory.getLog(ReloadableResourcePropertySource.class);

    private Properties properties;

    public ReloadableResourcePropertySource(String name, EncodedResource source) {
        super(name, source);
        reload();
    }

    public boolean reload() {
        try {
            Properties properties = PropertiesLoaderUtils.loadProperties(getSource());
            boolean updated = !properties.equals(this.properties);
            this.properties = properties;
            if (log.isTraceEnabled()) {
                log.trace("Reloading properties " + name + ". Updated: " + updated);
            }
            return updated;
        } catch (IOException e) {
            log.error("Error loading properties " + name, e);
            return false;
        }
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public Object getProperty(String name) {
        return properties.getProperty(name);
    }
}
