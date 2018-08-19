package com.github.mpashka.spring.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * List of properties that are to be reloaded on configuration update
 *
 * @author Pavel Moukhataev
 */
class ReloadableBeanPropertyRepository {

    private static final Log log = LogFactory.getLog(ReloadableBeanPropertyRepository.class);

    private Map<String, BeanProperties> beansProperties = new HashMap<>();
    private ConfigurableListableBeanFactory beanFactory;
    private BeanExpressionContext beanExpressionContext;

    void initExpressionParser(ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        this.beanExpressionContext = new BeanExpressionContext(beanFactory, null);
    }

    boolean isEmpty() {
        return beansProperties.isEmpty();
    }

    BeanProperties getReloadableBeanProperties(String beanName) {
        BeanProperties properties = beansProperties.get(beanName);
        if (properties == null) {
            properties = new BeanProperties(beanName);
            beansProperties.put(beanName, properties);
        }
        return properties;
    }

    /**
     *
     */
    void updateBeans() {
        if (log.isDebugEnabled()) {
            log.debug("Update beans from repository: " + this);
        }
        for (BeanProperties beanProperties : beansProperties.values()) {
            beanProperties.update();
        }
    }

    void initProperties(Object bean, String beanName) {
        BeanProperties beanProperties = beansProperties.get(beanName);
        if (beanProperties != null) {
            beanProperties.initProperties(bean);
        }
    }

    /**
     * Check if some beans were not initialized
     */
    public void findNonInitBeans() {
        for (BeanProperties beanProperties : beansProperties.values()) {
            if (beanProperties.bean == null) {
                log.error("Bean was not initialized " + beanProperties.beanName + " for config update");
            }
        }
    }


    class BeanProperties {
        private String beanName;
        private List<BeanProperty> properties = new ArrayList<>();
        private Object bean;


        BeanProperties(String beanName) {
            this.beanName = beanName;
        }

        void add(String propertyName, String propertyValue, boolean reference, String resolvedValue, PropertyValue definition, Method method, Field field) {
            properties.add(new BeanProperty(this, propertyName, propertyValue, reference, resolvedValue, definition, method, field));
        }

        void update() {
            if (log.isTraceEnabled()) {
                log.trace("Updating beanProperties " + beanName);
            }
            for (BeanProperty property : this.properties) {
                try {
                    property.update();
                } catch (Exception e) {
                    log.error("Internal error beanProperties " + bean + " property " + property.name + " update", e);
                }
            }
        }

        void initProperties(Object bean) {
            this.bean = bean;
            for (BeanProperty property : properties) {
                property.init();
            }
        }
    }


    class BeanProperty {
        private final BeanProperties beanProperties;
        private final String definedValue;
        private final String name;
        private String oldValue;
        private Method writeMethod;
        private Field field;
        private Class<?> type;
        private final boolean reference;
        private final PropertyValue definition;

        /**
         * Create beanProperties property representation that is used for property update.
         *
         * Can be created from XML
         * or from annotations
         * @param reference if this is XML property ref
         * @param resolvedValue is null for XML property
         * @param definition is null for annotation property
         * @param method setter method for Value annotated property
         * @param field property field for Value annotated property
         */
        BeanProperty(BeanProperties beanProperties, String name, String definedValue, boolean reference, String resolvedValue, PropertyValue definition, Method method, Field field) {
            this.beanProperties = beanProperties;
            this.name = name;
            this.definedValue = definedValue;
            this.reference = reference;
            this.oldValue = resolvedValue;
            this.definition = definition;
            this.writeMethod = method;
            this.field = field;

            initType();
        }

        /**
         * See {@link org.springframework.beans.factory.support.DefaultListableBeanFactory#doResolveDependency}
         */
        void update() {
            String value = beanFactory.resolveEmbeddedValue(this.definedValue);
            if (log.isTraceEnabled()) {
                log.trace("    " + name + ":" + definedValue + "=" + oldValue + "->" + value);
            }
            if (equals(value, oldValue)) {
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("    Bean property set [" + this.beanProperties.beanName + "/" + name + "] = " + oldValue + " -> " + value);
            }

            oldValue = value;

            setBeanPropertyValue(bean(), value);
        }

        private boolean equals(String value, String oldValue) {
            return value == null ? oldValue == null : value.equals(oldValue);
        }

        private void setBeanPropertyValue(Object bean, String value) {
            try {

                Object valueObject = beanFactory.getBeanExpressionResolver().evaluate(value, beanExpressionContext);

                if (reference) {
                    valueObject = beanFactory.getBean(String.valueOf(valueObject));
                } else {
                    valueObject = beanFactory.getTypeConverter().convertIfNecessary(valueObject, type);
                }

                if (writeMethod != null) {
                    writeMethod.invoke(bean, valueObject);
                } else if (field != null) {
                    field.set(bean, valueObject);
                } else {
                    log.error("Neither field nor method were found for beanProperties " + this.beanProperties.beanName + " property " + name + " to set definedValue " + value);
                }
            } catch (Exception e) {
                log.error("Can't evaluate expression beanProperties " + this.beanProperties.beanName + "." + name + " -> '" + value + "':" + type, e);
            }
        }

        void init() {
            try {
                resolveOriginalXmlPropertyValue();

                if (field == null && writeMethod == null) {
                    initPropertyAccessor();
                }
            } catch (Exception e) {
                log.error("Internal error init beanProperties " + this.beanProperties.beanName + " property " + name + " accessor", e);
            }
        }

        /**
         * Find original property definedValue for XML property
         */
        private void resolveOriginalXmlPropertyValue() {
            if (definition != null) {
                Object value = definition.getValue();
                if (value instanceof TypedStringValue) {
                    TypedStringValue typedStringValue = (TypedStringValue) value;
                    oldValue = beanFactory.resolveEmbeddedValue(typedStringValue.getValue());
                } else if (value instanceof RuntimeBeanReference) {
                    RuntimeBeanReference beanReference = (RuntimeBeanReference) value;
                    oldValue = beanFactory.resolveEmbeddedValue(beanReference.getBeanName());
                } else {
                    log.error("Can't recognize definedValue type " + beanProperties.beanName + "." + name + ": " + value + "(" + (value == null ? "null" : value.getClass().getName()) + ")");
                }
            }
        }

        /**
         * This is for XML properties
         */
        private void initPropertyAccessor() {
            // Try method
            String methodName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
            Class<?> beanClass = bean().getClass();
            ReflectionUtils.doWithMethods(beanClass, m -> {
                        writeMethod = m;
                        type = writeMethod.getParameterTypes()[0];
                        ReflectionUtils.makeAccessible(writeMethod);
                    }
                    , m -> m.getName().equals(methodName) && m.getParameterCount() == 1);
            if (writeMethod != null) {
                return;
            }

            // Try field
            field = ReflectionUtils.findField(beanClass, name);
            if (field != null) {
                type = this.field.getType();
                ReflectionUtils.makeAccessible(this.field);
                return;
            }

            log.error("Neither field nor method were found for beanProperties " + beanProperties.beanName + " property " + name);
        }

        private void initType() {
            if (writeMethod != null) {
                type = writeMethod.getParameterTypes()[0];
            } else if (field != null) {
                type = field.getType();
            }
        }

        private Object bean() {
            return beanProperties.bean;
        }
    }

}
