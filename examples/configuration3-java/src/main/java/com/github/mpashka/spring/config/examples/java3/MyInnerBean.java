package com.github.mpashka.spring.config.examples.java3;

import org.springframework.beans.factory.annotation.Value;

public class MyInnerBean {

    @Value("${inner1.string}")
    private String innerString;

    @Value("${inner1.int}")
    private int innerInt;

    public void setInnerString(String innerString) {
        this.innerString = innerString;
    }

    public void setInnerInt(int innerInt) {
        this.innerInt = innerInt;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MyInnerBean{");
        sb.append("innerString='").append(innerString).append('\'');
        sb.append(", innerInt=").append(innerInt);
        sb.append('}');
        return sb.toString();
    }
}
