package springroll.framework.connector;

import org.springframework.web.reactive.socket.WebSocketMessage;

public interface Marshaller {

    WebSocketMessage marshal(Object event);

    Object unmarshal(WebSocketMessage webSocketMessage);

    NormalizedMessage normalize(WebSocketMessage webSocketMessage);

}
