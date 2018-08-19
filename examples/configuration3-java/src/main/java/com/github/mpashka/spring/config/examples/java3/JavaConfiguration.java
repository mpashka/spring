package com.github.mpashka.spring.config.examples.java3;


import com.github.mpashka.spring.config.EnvironmentConfigurationSource;
import com.github.mpashka.spring.config.ReloadableConfigurationSupport;
import com.github.mpashka.spring.config.ReloadableResourcePropertySourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.concurrent.ScheduledExecutorFactoryBean;

import java.util.concurrent.ScheduledExecutorService;

@Configuration
@ComponentScan("com.github.mpashka.spring.config.examples.java3")
@PropertySource(
        value = {
                "file:tmp/props1.properties",
                "file:tmp/props2.properties"
        },
        factory = ReloadableResourcePropertySourceFactory.class
)
public class JavaConfiguration {

    @Bean
    public JavaBean myJavaBean1() {
        return new JavaBean();
    }

    @Bean
    public MyInnerBean myInnerBean() {
        return new MyInnerBean();
    }

    @Bean
    public PropertiesListener propertiesListener() {
        return new PropertiesListener();
    }

    /**
     * Property placeholder configurer needed to process @Value annotations
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public ScheduledExecutorFactoryBean scheduledExecutor() {
        ScheduledExecutorFactoryBean scheduledExecutorFactoryBean = new ScheduledExecutorFactoryBean();
        scheduledExecutorFactoryBean.setThreadNamePrefix("Periodical-Tasks-");
        scheduledExecutorFactoryBean.setWaitForTasksToCompleteOnShutdown(true);
        scheduledExecutorFactoryBean.setContinueScheduledExecutionAfterException(true);
        return scheduledExecutorFactoryBean;
    }

    @Bean
    public EnvironmentConfigurationSource configurationSource(ScheduledExecutorService executorService) {
        return new EnvironmentConfigurationSource(executorService, 3000);
    }

    @Bean
    public ReloadableConfigurationSupport reloadableConfigurationSupport() {
        return new ReloadableConfigurationSupport();
    }
}
