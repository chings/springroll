package springroll.framework.connector;

import org.springframework.web.reactive.socket.WebSocketMessage;

public interface Marshaller {

    WebSocketMessage marshal(Object message);

    SemiMessage unmarshal(WebSocketMessage rawMessage);

    Object unmarshal(SemiMessage semiMessage, Class<?> messageClass);

}
