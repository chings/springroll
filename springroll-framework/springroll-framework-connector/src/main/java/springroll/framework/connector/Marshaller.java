package springroll.framework.connector;

import org.springframework.web.reactive.socket.WebSocketMessage;

public interface Marshaller {

    WebSocketMessage marshal(Object event);

    NormalizedMessage normalize(WebSocketMessage rawMessage);

    Object unmarshal(NormalizedMessage message, Class<?> finalClass);

}
