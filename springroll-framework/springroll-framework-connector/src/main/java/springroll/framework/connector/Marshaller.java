package springroll.framework.connector;

import org.springframework.web.reactive.socket.WebSocketMessage;

public interface Marshaller {

    String marshal(Object message);

    SemiMessage unmarshal(WebSocketMessage rawMessage);

    Object unmarshal(Class<?> messageClass, Object payload);

}
