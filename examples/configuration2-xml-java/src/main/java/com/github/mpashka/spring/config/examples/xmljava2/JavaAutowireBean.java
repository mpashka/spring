package com.github.mpashka.spring.config.examples.xmljava2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("myAutowireBean")
public class JavaAutowireBean {
    @Value("${javaabean1.string}")
    private String stringProperty;

    @Value("${javaabean1.int}")
    private int intProperty;

    @Autowired
    @Qualifier("myInner3")
    private MyInnerBean myInnerBean;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JavaAutowireBean{");
        sb.append("stringProperty='").append(stringProperty).append('\'');
        sb.append(", intProperty=").append(intProperty);
        sb.append(", myInnerBean=").append(myInnerBean);
        sb.append('}');
        return sb.toString();
    }
}
