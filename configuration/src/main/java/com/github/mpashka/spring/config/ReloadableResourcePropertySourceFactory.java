package com.github.mpashka.spring.config;


import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;

/**
 * Factory for the {@link PropertySource}. Can be used in java spring configuration
 * in {@link org.springframework.context.annotation.PropertySource} section.
 */
public class ReloadableResourcePropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        return new ReloadableResourcePropertySource(name == null ? getName(resource) : name, resource);
    }

    private String getName(EncodedResource resource) throws IOException {
        return resource.getResource().getURI().toString();
    }
}
