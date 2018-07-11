package springroll.framework.connector;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import springroll.framework.connector.protocol.Connected;
import springroll.framework.connector.util.Classes;
import springroll.framework.core.ActorRegistry;
import springroll.framework.core.Actors;

import java.security.Principal;

import static springroll.framework.core.Actors.tell;

public class WebSocketConnector implements WebSocketHandler, InitializingBean {

    @Autowired
    ActorSystem actorSystem;

    @Autowired
    ActorRegistry actorRegistry;

    @Autowired
    Marshaller marshaller;

    ActorRef connectionMaster;

    @Override
    public void afterPropertiesSet() throws Exception {
        connectionMaster = Actors.spawn(actorSystem, "connections", ConnectionMaster.class, actorRegistry);
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        HandshakeInfo handshakeInfo = session.getHandshakeInfo();
        Principal principal = handshakeInfo.getPrincipal().block();
        if(principal == null) return Mono.error(new IllegalAccessException("principal not present"));

        Flux<MessageDelivery> source = session.receive()
                .map(marshaller::normalize)
                .map(this::delivery)
                .filter(express -> express != null);
        UnicastProcessor<Object> output = UnicastProcessor.create();
        FluxSink<Object> sink = output.sink();
        tell(connectionMaster, new Connected(principal.getName(), source, sink));
        return session.send(output.map(marshaller::marshal));
    }

    public MessageDelivery delivery(NormalizedMessage normalizedMessage) {
        String to = normalizedMessage.getTo();
        String type = normalizedMessage.getType();
        if(to == null || type == null) return null;
        ActorRef toRef = actorRegistry.get(to);
        if(toRef == null) return null;
        Class<?> messageClass = Classes.guess(type);
        if(messageClass == null) return null;
        Object message = marshaller.unmarshal(normalizedMessage, messageClass);
        if(message == null) return null;
        return new MessageDelivery(message, toRef);
    }

}
