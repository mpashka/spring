package com.github.mpashka.spring.config.examples.xmljava2;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JavaConfiguration {

    @Bean
    public JavaBean myJavaBean1(@Qualifier("myInner2") MyInnerBean myInnerBean) {
        return new JavaBean(myInnerBean);
    }

}
