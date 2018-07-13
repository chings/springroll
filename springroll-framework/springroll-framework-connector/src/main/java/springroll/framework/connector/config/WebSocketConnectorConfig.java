package springroll.framework.connector.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import springroll.framework.connector.FastJsonMarshaller;
import springroll.framework.connector.Marshaller;
import springroll.framework.connector.WebSocketConnector;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebSocketConnectorConfig {

    @Bean
    public Marshaller marshaller() {
        return new FastJsonMarshaller();
    }

    @Bean
    public WebSocketConnector websocketConnector() {
        return new WebSocketConnector();
    }

    @Value("${springroll.connector.path:/connect}")
    String path;

    @Bean
    public HandlerMapping webSocketMapping() {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put(path, websocketConnector());
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(map);
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

}
