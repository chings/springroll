package springroll.framework.connector;

import akka.actor.ActorSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

public class WebSocketConnector implements WebSocketHandler {

    @Autowired
    ActorSystem actorSystem;


    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        return null;
    }

}
