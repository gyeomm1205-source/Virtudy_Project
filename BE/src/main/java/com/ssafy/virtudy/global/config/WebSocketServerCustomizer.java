package com.ssafy.virtudy.global.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebSocketServerCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Value("${websocket.port}")
    private int websocketPort;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setPort(websocketPort);
        factory.addAdditionalTomcatConnectors(connector);
    }
}
