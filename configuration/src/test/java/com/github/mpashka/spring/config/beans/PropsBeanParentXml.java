package com.github.mpashka.spring.config.beans;

import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 *
 * @author Pavel Moukhataev
 */
public abstract class PropsBeanParentXml {

    private String p3;
    private int p3Count;

    private int p4;
    private int p4Count;

    private String s5;
    private int s5Count;

    private String p6ref;
    private int p6refCount;

    private PropsBeanValueXml childXml;

    private List<PropsBeanValueXml> childXmlList;

    private String checkNoGetter;

    @Value("${valueProperty}")
    private String checkValueProperty;

    private String checkValueSetter;

    public String getP3() {
        return p3;
    }

    public void setP3(String p3) {
        this.p3 = p3;
        p3Count++;
    }

    public int getP3Count() {
        return p3Count;
    }

    public int getP4() {
        return p4;
    }

    public void setP4(int p4) {
        this.p4 = p4;
        p4Count++;
    }

    public int getP4Count() {
        return p4Count;
    }

    public String getS5() {
        return s5;
    }

    public void setS5(String s5) {
        this.s5 = s5;
        s5Count++;
    }

    public int getS5Count() {
        return s5Count;
    }

    public String getP6ref() {
        return p6ref;
    }

    public void setP6ref(String p6ref) {
        this.p6ref = p6ref;
        p6refCount++;
    }

    public int getP6refCount() {
        return p6refCount;
    }

    public PropsBeanValueXml getChildXml() {
        return childXml;
    }

    public void setChildXml(PropsBeanValueXml childXml) {
        this.childXml = childXml;
    }

    public List<PropsBeanValueXml> getChildXmlList() {
        return childXmlList;
    }

    public void setChildXmlList(List<PropsBeanValueXml> childXmlList) {
        this.childXmlList = childXmlList;
    }

    /**
     * Note: method named "nogetter" because I found problem with unrecognized
     * property if getter is not present
     */
    public String nogetterCheckNoGetter() {
        return checkNoGetter;
    }

    public void setCheckNoGetter(String checkNoGetter) {
        this.checkNoGetter = checkNoGetter;
    }

    public String nogetterCheckValueProperty() {
        return checkValueProperty;
    }

    public String getCheckValueSetter() {
        return checkValueSetter;
    }

    @Value("${valueSetter}")
    public void setCheckValueSetter(String checkValueSetter) {
        this.checkValueSetter = checkValueSetter;
    }
}
