package springroll.framework.connector;

import akka.actor.ActorRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import springroll.framework.connector.protocol.Connected;
import springroll.framework.connector.protocol.Disconnected;
import springroll.framework.connector.protocol.Kick;
import springroll.framework.core.ActorRegistry;
import springroll.framework.core.Actorlet;
import springroll.framework.core.annotation.State;
import springroll.framework.protocol.JoinMessage;
import springroll.framework.protocol.UnjoinMessage;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

import static springroll.framework.connector.Frame.Method;

public class Connection extends Actorlet {
    private static Logger log = LoggerFactory.getLogger(Connection.class);

    @Autowired
    ActorRegistry actorRegistry;

    @Autowired
    FrameProtocol frameProtocol;

    String principalName;
    Flux<Frame> source;
    FluxSink<Frame> sink;

    Set<ActorRef> joinedActors = new HashSet<>();

    public void on(Connected connected) {
        principalName = connected.getPrincipalName();
        source = connected.getSource();
        sink = connected.getSink();
        source.subscribe(this::handleNext, this::handleError, this::handleComplete);
        become(ConnectedState.class);
    }

    @State
    public class ConnectedState {

        public void on(Kick message) {
            Frame frame = frameProtocol.marshal(message);
            frame.setMethod(Method.ERROR);
            sink.next(frame);
            sink.complete();
        }

        public void on(Object message, ActorRef from) {
            Frame frame = frameProtocol.marshal(message);
            frame.setMethod(Method.TELL);
            frame.setUri(ActorRegistry.path(from));
            sink.next(frame);
            postSendOut(message, from);
        }

    }

    public void handleNext(Frame frame) {
        switch(frame.getMethod()) {
            case PING:
                Frame pongFrame = new Frame(Method.PONG);
                if(StringUtils.hasText(frame.getSerialNo())) {
                    pongFrame.setReSerialNo(frame.getSerialNo());
                }
                sink.next(pongFrame);
                break;
            case TELL:
                ActorRef to = actorRegistry.resovle(frame.getUri());
                String namespace = actorRegistry.namespace(frame.getUri());
                Object message = frameProtocol.unmarshal(frame, namespace);
                preSendIn(to, message);
                tell(to, message);
                break;
            case ASK:
                to = actorRegistry.resovle(frame.getUri());
                namespace = actorRegistry.namespace(frame.getUri());
                message = frameProtocol.unmarshal(frame, namespace);
                preSendIn(to, message);
                ask(to, message, reply -> {
                    if(reply instanceof Throwable) {
                        Frame errorFrame = new Frame(Method.ERROR);
                        errorFrame.setUri(frame.getUri());
                        errorFrame.setReSerialNo(frame.getSerialNo());
                        sink.next(errorFrame);
                    } else {
                        Frame replyFrame = frameProtocol.marshal(reply);
                        replyFrame.setMethod(Method.REPLY);
                        replyFrame.setUri(frame.getUri());
                        replyFrame.setReSerialNo(frame.getSerialNo());
                        sink.next(replyFrame);
                        postSendOut(message, to);
                    }
                });
                break;
            case DISCONNECT:
                sink.complete();
        }
    }

    public void preSendIn(ActorRef to, Object message) {
        if(message instanceof JoinMessage) {
            joinedActors.add(to);
        } else if(message instanceof UnjoinMessage) {
            joinedActors.remove(to);
        }
    }

    public void postSendOut(Object message, ActorRef from) {
        preSendIn(from, message);
    }

    public void handleError(Throwable x) {
        log.error("Ugh! {}", x.getMessage(), x);
        notifyDisconnected(x.getMessage());
        terminate();
    }

    public void handleComplete() {
        notifyDisconnected(null);
        terminate();
    }

    public void notifyDisconnected(@Nullable String reason) {
        Disconnected disconnected = new Disconnected(principalName, reason);
        tell(getContext().getParent(), disconnected);
        for(ActorRef actor : joinedActors) {
            tell(actor, disconnected);
        }
    }

}
