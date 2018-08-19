package com.github.mpashka.spring.config.examples.xml1;

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
        copyFromClasspath(destDir, "props2.properties");
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("context-example1.xml");

        MyBean1 myBean1 = applicationContext.getBean("myBean1", MyBean1.class);
        log.info("Bean succesfully initialized: {}", myBean1);
        log.info("Change prop file {} and press enter to check new bean status", destDir.getCanonicalPath());
        Scanner scanner = new Scanner(System.in);
        //noinspection InfiniteLoopStatement
        while (true) {
            scanner.nextLine();
            log.info("New bean status: {}", myBean1);
        }
    }

    private static File copyFromClasspath(File destDir, String fileName) throws IOException {
        File file = new File(destDir, fileName);
        FileUtils.writeByteArrayToFile(file, IOUtils.toByteArray(ClassLoader.getSystemResource(fileName)));
        return file;
    }
}
