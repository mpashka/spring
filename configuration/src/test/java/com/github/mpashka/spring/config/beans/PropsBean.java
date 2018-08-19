package com.github.mpashka.spring.config.beans;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * PropsTest
 *
 * @author Pavel Moukhataev
 */
@Component
public class PropsBean {

    @Value("${p1}")
    private String p1;
    @Value("${p2}")
    private String p2;
    @Value("${pd1}")
    private double pd1;
    @Value("${pd2}")
    private int pd2;
    @Value("${s5}")
    private String s5;
    @Value("#{${p6ref}}")
    private String p6ref;
    @Value("#{ref1.length() - ${p8i}}")
    private int p8spel;

    public String getP1() {
        return p1;
    }

    public String getP2() {
        return p2;
    }

    public double getPd1() {
        return pd1;
    }

    public int getPd2() {
        return pd2;
    }

    public String getS5() {
        return s5;
    }

    public String getP6ref() {
        return p6ref;
    }

    public int getP8spel() {
        return p8spel;
    }
}
