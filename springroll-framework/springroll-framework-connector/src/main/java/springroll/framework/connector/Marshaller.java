package springroll.framework.connector;

import org.springframework.web.reactive.socket.WebSocketMessage;

public interface Marshaller {

    String marshal(Object message);

    SemiMessage normalize(WebSocketMessage rawMessage);

    Object unmarshal(SemiMessage semiMessage);

}
