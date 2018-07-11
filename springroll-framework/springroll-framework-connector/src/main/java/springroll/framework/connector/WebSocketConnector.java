package springroll.framework.connector;

import akka.actor.ActorSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

public class WebSocketConnector implements WebSocketHandler {

    ActorSystem actorSystem;

    @Autowired
    public void setActorSystem(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return Mono.<Void>empty();
    }

}
