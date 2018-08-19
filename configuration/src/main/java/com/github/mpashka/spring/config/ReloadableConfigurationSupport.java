package com.github.mpashka.spring.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.ManagedProperties;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * Spring bean. Find and store configurable properties from XML files and java annotations.
 * Listens to configuration update event and calls properties updates.
 *
 * See {@link #setOrder(int)} for order notes
 *
 * @implNote following scenarios are still not supported -> re-buildable collections if the property placeholder specifies item value or item is another bean reference
 * @author Pavel Moukhataev
 */
public class ReloadableConfigurationSupport implements PriorityOrdered, BeanFactoryPostProcessor, ApplicationListener<ConfigurationUpdatedEvent> {

    private static final Log log = LogFactory.getLog(ReloadableConfigurationSupport.class);


    /**
     * todo [3] property and expression part should be taken from spring
     */
    private static final String CONFIGURABLE_PROPERTY_PART = "${";
    private static final String CONFIGURABLE_EXPRESSION_PART = "#{";


    private ReloadableBeanPropertyRepository reloadableBeanPropertyRepository = new ReloadableBeanPropertyRepository();

    /**
     * Should go before {@link PropertyResourceConfigurer#order}
     */
    private int order = Ordered.LOWEST_PRECEDENCE-1;
    private ConfigurableListableBeanFactory beanFactory;


    @Override
    public int getOrder() {
        return order;
    }

    /**
     * Used to set order. This order must be less then PropertySourcesPlaceholderConfigurer.order
     *
     * Note: both {@link PropertyResourceConfigurer} and {@link ReloadableConfigurationSupport} start business logic
     * on {@link BeanFactoryPostProcessor#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)}
     * So this bean must be initiated before {@link org.springframework.context.support.PropertySourcesPlaceholderConfigurer}.
     *
     * @see EnvironmentConfigurationSource#setOrder(int)
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Called after bean factory definition, before instantiation
     * This must be called before {@link PropertyPlaceholderConfigurer#postProcessBeanFactory(ConfigurableListableBeanFactory)}
     * Scans all bean definitions and initializes re-loadable property lookup for all properties configured using placeholders
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;

        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);

            if (!beanDefinition.isAbstract()) {
                scanBeanDefinition(beanName, beanDefinition);
            }
        }

        reloadableBeanPropertyRepository.initExpressionParser(beanFactory);
        addSpringBeanPostProcessor(beanFactory);

        if (reloadableBeanPropertyRepository.isEmpty()) {
            log.debug("XML configuration properties are empty. This is normal for java spring configuration. For XML config can be caused by invalid order - less then PropertyResourceConfigurer.");
        }
    }

    /**
     * Parse bean definitions for beans and embedded beans - this is for XML config
     *
     * Note: see list of all value types {@link org.springframework.beans.factory.support.BeanDefinitionValueResolver#resolveValueIfNecessary}
     */
    private void scanBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanDefinition.getPropertyValues()
                .getPropertyValueList()
                .forEach(propertyValue -> processProperty(beanName, propertyValue));
        scanParentDefinition(beanName, beanDefinition);
    }

    //scan parent definitions and build reloadable properties under child bean's name
    private void scanParentDefinition(String childBeanName, BeanDefinition childDefinition) {
        if (childDefinition.getParentName() != null) {
            BeanDefinition parentDefinition = beanFactory.getBeanDefinition(childDefinition.getParentName());

            if (parentDefinition != null) {
                scanBeanDefinition(childBeanName, parentDefinition);
            }
        }
    }

    private void processProperty(String beanName, PropertyValue propertyValue) {
        Object value = propertyValue.getValue();
        if (value instanceof TypedStringValue) {
            TypedStringValue typedStringValue = (TypedStringValue) value;
            if (isExpression(typedStringValue.getValue())) {
                ReloadableBeanPropertyRepository.BeanProperties reloadableBeanProperties = reloadableBeanPropertyRepository.getReloadableBeanProperties(beanName);
                reloadableBeanProperties.add(propertyValue.getName(), typedStringValue.getValue(), false, null, propertyValue, null, null);
            }
        } else if (value instanceof RuntimeBeanReference) {
            RuntimeBeanReference beanReference = (RuntimeBeanReference) value;
            if (isExpression(beanReference.getBeanName())) {
                ReloadableBeanPropertyRepository.BeanProperties reloadableBeanProperties = reloadableBeanPropertyRepository.getReloadableBeanProperties(beanName);
                reloadableBeanProperties.add(propertyValue.getName(), beanReference.getBeanName(), true, null, propertyValue, null, null);
            }
        }  else if (value instanceof BeanDefinitionHolder){
            BeanDefinitionHolder beanDefinitionHolder = (BeanDefinitionHolder) value;
            scanBeanDefinition(beanDefinitionHolder.getBeanName(), beanDefinitionHolder.getBeanDefinition());
        } else  {
            checkIfManagedCollection(value);
        }
    }

    private static boolean isExpression(String value) {
        return value != null && (value.contains(CONFIGURABLE_PROPERTY_PART) || value.contains(CONFIGURABLE_EXPRESSION_PART));
    }

    private void checkIfManagedCollection(Object value) {
        if (value instanceof ManagedList || value instanceof ManagedSet) {
            processManagedCollection((Collection<?>) value);
        } else if (value instanceof ManagedMap || value instanceof ManagedProperties) {
            Map<?,?> map = (Map<?,?>)value;
            processManagedCollection(map.keySet());
            processManagedCollection(map.values());
        }
    }

    private void processManagedCollection(Collection<?> managedCollection) {
        managedCollection.forEach(item -> {
            if (item instanceof BeanDefinitionHolder){
                BeanDefinitionHolder beanDefinitionHolder = (BeanDefinitionHolder) item;
                scanBeanDefinition(beanDefinitionHolder.getBeanName(), beanDefinitionHolder.getBeanDefinition());
            } else {
                //just in case there are Lycanthropes among us
                checkIfManagedCollection(item);
            }
        });
    }

    /**
     * Note: we can't just implement {@link BeanPostProcessor} interface cause it wont be applied
     * to singleton beans. So we need to register listener in beanFactory
     */
    private void addSpringBeanPostProcessor(ConfigurableListableBeanFactory beanFactory) {
        beanFactory.addBeanPostProcessor(new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                return bean;
            }

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                try {
                    afterBeanInitialization(bean, beanName);
                } catch (Exception e) {
                    log.error("Error init bean " + beanName, e);
                }
                return bean;
            }
        });
    }

    /**
     * Is called by BeanPostProcessor#postProcessAfterInitialization
     * Parse annotations, init beans
     */
    private void afterBeanInitialization(Object bean, String beanName) throws BeansException {
        parseAnnotatedProperties(bean, beanName);

        // Put original property values to prevent reloading during first update
        reloadableBeanPropertyRepository.initProperties(bean, beanName);
    }

    private void parseAnnotatedProperties(Object bean, String beanName) {
        ReflectionUtils.doWithFields(bean.getClass(), field -> addAnnotatedBeanProperty(beanName, null, field));

        ReflectionUtils.doWithMethods(bean.getClass(), method -> {
            if (method.getParameterCount() == 1) {
                addAnnotatedBeanProperty(beanName, method, null);
            }
        });
    }

    /**
     * Add {@link Value} annotated bean field or method
     */
    private void addAnnotatedBeanProperty(String beanName, Method method, Field field) {
        AccessibleObject annotated = method != null ? method : field;
        Member member = method != null ? method : field;
        Value valueAnnotation = annotated.getAnnotation(Value.class);
        String value;
        if (valueAnnotation != null && isExpression(value = valueAnnotation.value())) {
            ReloadableBeanPropertyRepository.BeanProperties beanProperties = reloadableBeanPropertyRepository.getReloadableBeanProperties(beanName);
            String fieldName = member.getName();
            String resolvedValue = beanFactory.resolveEmbeddedValue(value);
            annotated.setAccessible(true);
            beanProperties.add(fieldName, value, false, resolvedValue, null, method, field);
        }
    }


    @Override
    public void onApplicationEvent(ConfigurationUpdatedEvent event) {
        reloadableBeanPropertyRepository.updateBeans();
        log.debug("Properties reloaded");
    }
}
