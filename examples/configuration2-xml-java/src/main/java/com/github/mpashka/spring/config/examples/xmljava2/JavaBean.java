package com.github.mpashka.spring.config.examples.xmljava2;

import org.springframework.beans.factory.annotation.Value;

public class JavaBean {

    private MyInnerBean inner1;

    @Value("${javabean1.string}")
    private String stringProperty;

    private int intProperty;
    private double doubleProperty;

    public JavaBean(MyInnerBean inner1) {
        this.inner1 = inner1;
    }

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
        sb.append("inner1=").append(inner1);
        sb.append(", stringProperty='").append(stringProperty).append('\'');
        sb.append(", intProperty=").append(intProperty);
        sb.append(", doubleProperty=").append(doubleProperty);
        sb.append('}');
        return sb.toString();
    }
}
