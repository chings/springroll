package springroll.framework.connector;

import akka.actor.ActorRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import springroll.framework.connector.protocol.Connected;
import springroll.framework.core.annotation.ActorBean;

import java.rmi.server.UID;
import java.security.Principal;

import static springroll.framework.core.Actors.tell;

public class WebSocketConnector implements WebSocketHandler {
    private static Logger log = LoggerFactory.getLogger(WebSocketConnector.class);

    @ActorBean("connections")
    ActorRef connectionMaster;

    @Autowired
    FrameProtocol frameProtocol;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        HandshakeInfo handshakeInfo = session.getHandshakeInfo();
        Principal principal = handshakeInfo.getPrincipal().block();
        //if(principal == null) return Mono.error(new IllegalAccessException("principalName not present"));

        Flux<Frame> source = session.receive()
                .map(webSocketMessage -> webSocketMessage.getPayloadAsText())
                .map(frameProtocol::unseralize);

        UnicastProcessor<Frame> processor = UnicastProcessor.create();
        FluxSink<Frame> sink = processor.sink();

        tell(connectionMaster, new Connected(new UID().toString(), source, sink));
        return session.send(processor.map(frameProtocol::serialize).map(session::textMessage));
    }

}
