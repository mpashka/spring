package com.github.mpashka.spring.config.examples.xml1;

public class MyInnerBean {

    private String innerString;
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
