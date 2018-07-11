package springroll.framework.connector;

import akka.actor.ActorRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import springroll.framework.connector.protocol.Connected;
import springroll.framework.core.AgentActor;
import springroll.framework.core.annotation.ActorBean;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import static springroll.framework.core.Actors.tell;

public class WebSocketConnector implements WebSocketHandler {

    @ActorBean(ConnectionMaster.class)
    ActorRef connectionMaster;

    @Autowired
    Marshaller marshaller;

    Map<Class<? extends AgentActor>, List<String>> agentRegistrations;

    public void setAgentClasses(Class<? extends AgentActor>[] agentClasses) {
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        HandshakeInfo handshakeInfo = session.getHandshakeInfo();
        Principal principal = handshakeInfo.getPrincipal().block();
        if(principal == null) return Mono.error(new IllegalAccessException("principal not present"));

        Flux<NormalizedMessage> source = session.receive().map(marshaller::normalize);
        UnicastProcessor<Object> publisher = UnicastProcessor.create();
        FluxSink<Object> sink = publisher.sink();
        tell(connectionMaster, new Connected(principal.getName(), source, sink, agentRegistrations));
        return session.send(publisher.map(marshaller::marshal));
    }

}
