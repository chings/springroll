package springroll.framework.connector;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import reactor.util.function.Tuple2;
import springroll.framework.connector.protocol.Connected;
import springroll.framework.core.ActorRegistry;
import springroll.framework.core.Actors;
import springroll.framework.core.annotation.ActorBean;

import java.security.Principal;

import static springroll.framework.core.Actors.tell;

public class WebSocketConnector implements WebSocketHandler {

    @Autowired
    ActorSystem actorSystem;

    @Autowired
    ActorRegistry actorRegistry;

    @Autowired
    Marshaller marshaller;

    @ActorBean(ConnectionMaster.class)
    ActorRef connectionMaster;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        HandshakeInfo handshakeInfo = session.getHandshakeInfo();
        Principal principal = handshakeInfo.getPrincipal().block();
        if(principal == null) return Mono.error(new IllegalAccessException("principal not present"));

        Flux<SemiMessage> input = session.receive().map(marshaller::normalize);
        Flux<Tuple2<ActorRef, Object>> source = input.map(this::findRecipient).zipWith(input.map(marshaller::unmarshal))
                .filter(tuple2 -> tuple2.getT1() != null && tuple2.getT2() != null);

        UnicastProcessor<Object> output = UnicastProcessor.create();
        FluxSink<Object> sink = output.sink();

        tell(connectionMaster, new Connected(principal.getName(), source, sink));
        return session.send(output.map(marshaller::marshal).map(session::textMessage));
    }

    public ActorRef findRecipient(SemiMessage semiMessage) {
        String to = semiMessage.getTo();
        if(to == null) return null;
        return actorRegistry.get(to);
    }

}
