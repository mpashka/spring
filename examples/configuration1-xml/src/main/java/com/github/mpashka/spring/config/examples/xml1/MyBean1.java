package com.github.mpashka.spring.config.examples.xml1;

import org.springframework.beans.factory.annotation.Required;

public class MyBean1 {

    private String stringProperty;
    private int intProperty;

    private MyInnerBean inner1;
    private MyInnerBean inner2;

    public void setInner1(MyInnerBean inner1) {
        this.inner1 = inner1;
    }

    public void setInner2(MyInnerBean inner2) {
        this.inner2 = inner2;
    }

    @Required
    public void setStringProperty(String stringProperty) {
        this.stringProperty = stringProperty;
    }

    @Required
    public void setIntProperty(int intProperty) {
        this.intProperty = intProperty;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MyBean1{");
        sb.append("stringProperty='").append(stringProperty).append('\'');
        sb.append(", intProperty=").append(intProperty);
        sb.append(", inner1=").append(inner1);
        sb.append(", inner2=").append(inner2);
        sb.append('}');
        return sb.toString();
    }
}
