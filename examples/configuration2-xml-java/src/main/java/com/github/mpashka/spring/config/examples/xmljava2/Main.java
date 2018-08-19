package com.github.mpashka.spring.config.examples.xmljava2;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) throws Exception {
        File destDir = new File("tmp");
        destDir.mkdirs();
        FileUtils.forceDeleteOnExit(destDir);
        copyFromClasspath(destDir, "props1.properties");
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("context-example2.xml");

        Scanner scanner = new Scanner(System.in);
        //noinspection InfiniteLoopStatement
        while (true) {
            log.info("List beans");

            for (String beanDefinitionName : applicationContext.getBeanDefinitionNames()) {
                if (beanDefinitionName.startsWith("my")) {
                    Object bean = applicationContext.getBean(beanDefinitionName);
                    log.info("    {}: {}", beanDefinitionName, bean);
                }
            }

            scanner.nextLine();
        }
    }

    private static File copyFromClasspath(File destDir, String fileName) throws IOException {
        File file = new File(destDir, fileName);
        FileUtils.writeByteArrayToFile(file, IOUtils.toByteArray(ClassLoader.getSystemResource(fileName)));
        return file;
    }
}
