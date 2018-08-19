package com.github.mpashka.spring.config;

import com.github.mpashka.spring.config.utils.ContentServer;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class ContentServerTest {

    @Test
    public void test() throws Exception {
        ContentServer propertiesServer = new ContentServer("PropertiesServer", 8080);
        String content = "Hello\nworld!\n";
        propertiesServer.setContent("/props1.txt", content);
        URL url = new URL("http://localhost:8080/props1.txt");
        InputStream inputStream = url.openStream();
        Reader inReader = new InputStreamReader(inputStream);
        StringWriter out = new StringWriter();
        int inChar;
        while ((inChar = inReader.read()) != -1) {
            out.write(inChar);
        }
        inReader.close();
        out.close();
        String result = out.getBuffer().toString();
        assertEquals(content, result);
        propertiesServer.stop();
    }
}
