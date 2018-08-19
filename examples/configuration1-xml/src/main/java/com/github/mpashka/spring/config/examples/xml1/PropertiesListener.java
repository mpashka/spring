package com.github.mpashka.spring.config.examples.xml1;

import com.github.mpashka.spring.config.ConfigurationUpdatedEvent;
import com.github.mpashka.spring.config.ReloadableResourcePropertySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import java.util.Map;

public class PropertiesListener implements ApplicationListener<ConfigurationUpdatedEvent> {

    private static final Logger log = LoggerFactory.getLogger(PropertiesListener.class);

    @Override
    public void onApplicationEvent(ConfigurationUpdatedEvent event) {
        log.info("Configuration sources updated");
        for (ReloadableResourcePropertySource reloadableResourcePropertySource : event.getUpdatedSources()) {
            log.info("    {}", reloadableResourcePropertySource.getName());
//            reloadableResourcePropertySource.getProperty("aaa");
            log.info("    properties:");
            for (Map.Entry<Object, Object> objectObjectEntry : reloadableResourcePropertySource.getProperties().entrySet()) {
                log.info("        {} = {}", objectObjectEntry.getKey(), objectObjectEntry.getValue());
            }
        }
    }
}
