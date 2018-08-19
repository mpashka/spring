package com.github.mpashka.spring.config.examples.java3;

import org.springframework.beans.factory.annotation.Value;

public class JavaBean {

    @Value("${javabean1.string}")
    private String stringProperty;

    private int intProperty;
    private double doubleProperty;

    @Value("${javabean1.int}")
    public void setIntProperty(int intProperty) {
        this.intProperty = intProperty;
    }

    @Value("${javabean1.double:3.4}")
    public void setDoubleProperty(double doubleProperty) {
        this.doubleProperty = doubleProperty;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JavaBean{");
        sb.append("stringProperty='").append(stringProperty).append('\'');
        sb.append(", intProperty=").append(intProperty);
        sb.append(", doubleProperty=").append(doubleProperty);
        sb.append('}');
        return sb.toString();
    }
}
