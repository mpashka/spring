package com.github.mpashka.spring.config.utils;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class ContentServer {

    private final static Logger log = LoggerFactory.getLogger(ContentServer.class);


    private static final String DEFAULT_CONTENT_TYPE = "text/html";
    private volatile String contentType = DEFAULT_CONTENT_TYPE;
    private final Server server;
    private final Map<String, String> contentMap = new ConcurrentHashMap<>();

    public ContentServer(String name, int port) throws Exception {
        server = create(port);
        server.start();
        log.debug("Content Server '{}' has been started on port {}", name, port);
    }

    protected Server create(int port) {
        final Server contentServer = new Server(new QueuedThreadPool(5));
        ServerConnector connector = new ServerConnector(contentServer, 1, 1);
        connector.setPort(port);
        contentServer.setConnectors(new Connector[]{connector});

        Handler defaultHandler = new AbstractHandler() {

            @SuppressWarnings({"ConstantConditions", "ConstantConditions"})
            @Override
            public void handle(String path, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
                log.trace("Request received for path {}", path);
                String content = contentMap.get(request.getPathInfo());
                if (content != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType(contentType);
                    response.getOutputStream().write(content.getBytes());
                    response.getOutputStream().flush();
                    log.trace("Response sent for path {}, found {}", path, content != null);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    log.trace("Response sent for path {}, found {}", path, content != null);
                }
            }

        };
        contentServer.setHandler(defaultHandler);
        return contentServer;
    }

    public void setContent(String path, String content) {
        contentMap.put(path, content);
    }

    public void clearContent(String path) {
        contentMap.remove(path);
    }

    public void stop() {
        if (server != null) {
            try {
                server.stop();
            } catch (Exception ex) {
                throw new RuntimeException("An exception has occurred while stopping content server", ex);
            }
        }
    }


    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

}
