package com.github.mpashka.spring.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.ResourcePropertySource;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Checks spring environment, periodically updates configuration
 *
 */
public class EnvironmentConfigurationSource implements PriorityOrdered, BeanFactoryPostProcessor, EnvironmentAware, ApplicationContextAware {

    private static final Log log = LogFactory.getLog(EnvironmentConfigurationSource.class);

    private ConfigurableEnvironment environment;
    private ScheduledExecutorService executorService;
    private long periodMs;
    private List<String> sourceUrls;
    private List<ReloadableResourcePropertySource> sources;
    private boolean reuseEnvironment = true;
    private boolean reconfigureEnvironment;
    private int order = Ordered.LOWEST_PRECEDENCE - 2;
    private ConfigurableApplicationContext applicationContext;

    public EnvironmentConfigurationSource(ScheduledExecutorService executorService, long periodMs) {
        this(executorService, periodMs, null);
    }

    public EnvironmentConfigurationSource(ScheduledExecutorService executorService, long periodMs, List<String> sourceUrls) {
        this.executorService = executorService;
        this.periodMs = periodMs;
        this.sourceUrls = sourceUrls;
    }

    @Override
    public int getOrder() {
        return order;
    }

    /**
     * Should go before {@link PropertyResourceConfigurer#order} and before {@link ReloadableConfigurationSupport}
     * @param order spring init order
     * @see ReloadableConfigurationSupport#setOrder(int)
     */
    @SuppressWarnings("unused")
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (sources == null) {
            sources = new ArrayList<>();
        }
        if (reuseEnvironment) {
            reuseEnvironmentSources();
        }
        if (reconfigureEnvironment) {
            reconfigureEnvironmentSources();
        }
        if (sourceUrls != null) {
            addSourceUrls();
        }
        executorService.scheduleWithFixedDelay(this::updateSources, periodMs, periodMs, TimeUnit.MILLISECONDS);
    }

    private void addSourceUrls() {
        for (String sourceUrl : sourceUrls) {
            try {
                URI uri = URI.create(sourceUrl);
                ReloadableResourcePropertySource propertySource = new ReloadableResourcePropertySource(sourceUrl, new EncodedResource(new UrlResource(uri)));
                sources.add(propertySource);
                environment.getPropertySources().addLast(propertySource);
            } catch (MalformedURLException e) {
                log.error("Invalid environment URL: " + sourceUrl, e);
            }
        }
    }

    /**
     * Replace property source with reloadable
     */
    private void reconfigureEnvironmentSources() {
        MutablePropertySources propertySources = environment.getPropertySources();
        for (PropertySource<?> propertySource : propertySources) {
            if (propertySource instanceof ResourcePropertySource) {
                ResourcePropertySource original = (ResourcePropertySource) propertySource;
                String name = original.getName();
                // This is not clean. Check {@link ResourcePropertySource}, {@link org.springframework.core.io.UrlResource.getDescription}
                if (name.startsWith("URL [") && name.endsWith("]")) {
                    String urlStr = name.substring(5, name.length() - 1);
                    try {
                        URI uri = URI.create(urlStr);
                        ReloadableResourcePropertySource replacement = new ReloadableResourcePropertySource(name, new EncodedResource(new UrlResource(uri)));
                        propertySources.replace(name, replacement);
                        sources.add(replacement);
                    } catch (MalformedURLException e) {
                        log.error("Invalid environment URL: " + urlStr, e);
                    }
                }
            }
        }
    }

    private void reuseEnvironmentSources() {
        MutablePropertySources propertySources = environment.getPropertySources();
        for (PropertySource<?> propertySource : propertySources) {
            if (propertySource instanceof ReloadableResourcePropertySource) {
                ReloadableResourcePropertySource reloadableResourcePropertySource = (ReloadableResourcePropertySource) propertySource;
                sources.add(reloadableResourcePropertySource);
            }
        }
    }

    public void updateSources() {
        log.trace("Updating sources...");
        List<ReloadableResourcePropertySource> updatedSources = new ArrayList<>();
        for (ReloadableResourcePropertySource source : sources) {
            if (source.reload()) {
                updatedSources.add(source);
            }
        }
        if (!updatedSources.isEmpty() && applicationContext.isRunning()) {
            applicationContext.publishEvent(new ConfigurationUpdatedEvent(this, updatedSources));
        }
    }

    public EnvironmentConfigurationSource setReconfigureEnvironment(boolean reconfigureEnvironment) {
        this.reconfigureEnvironment = reconfigureEnvironment;
        return this;
    }

    public EnvironmentConfigurationSource setReuseEnvironment(boolean reuseEnvironment) {
        this.reuseEnvironment = reuseEnvironment;
        return this;
    }
}
