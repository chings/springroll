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
import springroll.framework.core.ActorRegistry;
import springroll.framework.core.Actors;
import springroll.framework.core.GenericActor;
import springroll.framework.core.annotation.At;
import springroll.framework.protocol.JoinMessage;
import springroll.framework.protocol.UnjoinMessage;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

import static springroll.framework.connector.Frame.Method;

public class Connection extends GenericActor {
    private static Logger log = LoggerFactory.getLogger(Connection.class);

    public static final String CONNECTING = At.BEGINNING;
    public static final String CONNECTED = "CONNECTED";

    @Autowired
    ActorRegistry actorRegistry;

    @Autowired
    FrameProtocol frameProtocol;

    String principalName;
    Flux<Frame> source;
    FluxSink<Frame> sink;

    Set<ActorRef> joinedActors = new HashSet<>();

    public String on(Connected connected) {
        principalName = connected.getPrincipalName();
        source = connected.getSource();
        sink = connected.getSink();
        source.subscribe(this::onNext, this::onError, this::onComplete);
        return CONNECTED;
    }

    @At(CONNECTED)
    public void on(Object message, ActorRef from) {
        Frame frame = frameProtocol.marshal(message);
        frame.setMethod(Method.TELL);
        frame.setUri(Actors.shortPath(from));
        sink.next(frame);
        postOutgo(message, from);
    }

    public void onNext(Frame frame) {
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
                Object message = frameProtocol.unmarshal(frame);
                preIncome(to, message);
                tell(to, message);
                break;
            case ASK:
                to = actorRegistry.resovle(frame.getUri());
                message = frameProtocol.unmarshal(frame);
                preIncome(to, message);
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
                        postOutgo(message, to);
                    }
                });
                break;
            case DISCONNECT:
                sink.complete();
        }
    }

    public void preIncome(ActorRef to, Object message) {
        if(message instanceof JoinMessage) {
            joinedActors.add(to);
        } else if(message instanceof UnjoinMessage) {
            joinedActors.remove(to);
        }
    }

    public void postOutgo(Object message, ActorRef from) {
        preIncome(from, message);
    }

    public void onError(Throwable x) {
        log.error("Ugh! {}", x.getMessage(), x);
        notifyDisconnected(x.getMessage());
        terminate();
    }

    public void onComplete() {
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
