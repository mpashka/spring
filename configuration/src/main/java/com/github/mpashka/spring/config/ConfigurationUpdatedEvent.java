package com.github.mpashka.spring.config;

import org.springframework.context.ApplicationEvent;

import java.util.List;

public class ConfigurationUpdatedEvent extends ApplicationEvent {

    private List<ReloadableResourcePropertySource> updatedSources;

    public ConfigurationUpdatedEvent(Object source, List<ReloadableResourcePropertySource> updatedSources) {
        super(source);
        this.updatedSources = updatedSources;
    }

    public List<ReloadableResourcePropertySource> getUpdatedSources() {
        return updatedSources;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConfigurationUpdatedEvent{");
        sb.append("updatedSources=").append(updatedSources);
        sb.append(", source=").append(source);
        sb.append('}');
        return sb.toString();
    }
}
