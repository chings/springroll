package springroll.framework.connector.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import springroll.framework.connector.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebSocketConnectorConfig {

    @Bean
    public FrameProtocol frameProtocol() {
        return new HttpLikeFrameProtocol();
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

    @Value("${springroll.connector.path:/roll}")
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
