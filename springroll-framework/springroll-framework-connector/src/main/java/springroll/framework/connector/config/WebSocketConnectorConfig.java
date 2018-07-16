package springroll.framework.connector.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import springroll.framework.connector.*;

import java.util.Collections;

@Configuration
public class WebSocketConnectorConfig {

    @Bean
    public FrameProtocol frameProtocol() {
        return new SpringRollFrameProtocol();
    }

    @Bean("connections")
    @Scope("prototype")
    public ConnectionMaster connectionMaster() {
        return new ConnectionMaster();
    }

    @Bean("connection")
    @Scope("prototype")
    public Connection connection() {
        return new Connection();
    }

    @Bean
    public WebSocketConnector websocketConnector() {
        return new WebSocketConnector();
    }

    @Value("${springroll.connector.endpoint:/roll}")
    String endpoint;

    @Bean
    public HandlerMapping webSocketMapping() {
        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setUrlMap(Collections.singletonMap(endpoint, websocketConnector()));
        handlerMapping.setOrder(10);    // must > 0, or... you can try
        return handlerMapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

}
