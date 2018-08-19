package com.github.mpashka.spring.config.beans;

import org.springframework.beans.factory.annotation.Value;

/**
 * PropsTest
 *
 * @author Pavel Moukhataev
 */
public class PropsBeanMix {

    @Value("${p1}")
    private String p1;
    @Value("${p2}")
    private String p2;
    @Value("${pd1}")
    private double pd1;
    @Value("${pd2}")
    private int pd2;
    @Value("${s5}")
    private String s5b;
    @Value("#{${p6ref}}")
    private String p6refb;

    private String p3;
    private int p3Count;

    private int p4;
    private int p4Count;

    private String s5x;
    private int s5Count;

    private String p6refx;
    private int p6refCount;


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

    public String getS5b() {
        return s5b;
    }

    public String getP6refb() {
        return p6refb;
    }

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

    public String getS5x() {
        return s5x;
    }

    public void setS5x(String s5x) {
        this.s5x = s5x;
        s5Count++;
    }

    public int getS5xCount() {
        return s5Count;
    }

    public String getP6refx() {
        return p6refx;
    }

    public void setP6refx(String p6refx) {
        this.p6refx = p6refx;
        p6refCount++;
    }

    public int getP6refxCount() {
        return p6refCount;
    }

}
