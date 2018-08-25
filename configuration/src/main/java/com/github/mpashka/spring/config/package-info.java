/**
 * Contains classes that provide reloadable properties support.
 *
 * Reloadable scheme:
 * {@link com.github.mpashka.spring.config.ReloadableResourcePropertySource} is used to load some remote resource.
 *
 * {@link com.github.mpashka.spring.config.EnvironmentConfigurationSource} checks resource changes.
 *
 * {@link org.springframework.beans.factory.config.PropertyPlaceholderConfigurer} is standard Spring class that
 * cycle through all spring beans, check if there are placeholders and use properties to resolve them. Also
 * define valueResolver for {@link org.springframework.beans.factory.config.ConfigurableBeanFactory}. Resolver
 * is used by java configurations, and by annotated properties.
 * See {@link org.springframework.beans.factory.config.PropertyPlaceholderConfigurer#processProperties(org.springframework.beans.factory.config.ConfigurableListableBeanFactory, java.util.Properties)},
 * {@link org.springframework.beans.factory.config.PlaceholderConfigurerSupport#doProcessProperties(org.springframework.beans.factory.config.ConfigurableListableBeanFactory, org.springframework.util.StringValueResolver)},
 * {@link org.springframework.beans.factory.config.ConfigurableBeanFactory#addEmbeddedValueResolver(org.springframework.util.StringValueResolver)}
 *
 * Embedded resolver is used later to update values after properties update - @link com.github.mpashka.spring.config.ReloadableBeanPropertyRepository.BeanProperty#setBeanPropertyValue(java.lang.Object, java.lang.String)
 *
 * {@link com.github.mpashka.spring.config.ReloadableConfigurationSupport} cycle through
 * {@link org.springframework.beans.factory.config.ConfigurableListableBeanFactory}, find all XML beans with properties with placeholders,
 * cycle through all Value annotated beans and save beans that require update into repository {@link com.github.mpashka.spring.config.ReloadableBeanPropertyRepository}.
 * Register itself as a listener on ReloadableProperties, and on property update cycle through all beans, check if update needed and updates bean value.
 *
 * {@link com.github.mpashka.spring.config.ReloadableBeanPropertyRepository} contains list of all beans that contains
 * configurable properties. For each bean contains field or method that is to be used to update property.
 *
 *
 * Events and listeners:
 * - {@link com.github.mpashka.spring.config.ConfigurationUpdatedEvent} - emitted on properties update
 *
 */
package com.github.mpashka.spring.config;
