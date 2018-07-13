package springroll.framework.connector;

import akka.actor.ActorRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.util.function.Tuple2;
import springroll.framework.connector.protocol.Connected;
import springroll.framework.connector.protocol.Disconnected;
import springroll.framework.core.GenericActor;
import springroll.framework.core.annotation.At;
import springroll.framework.protocol.JoinMessage;
import springroll.framework.protocol.UnjoinMessage;

import java.util.HashSet;
import java.util.Set;

public class Connection extends GenericActor {
    private static Logger log = LoggerFactory.getLogger(Connection.class);

    public static final String CONNECTING = At.BEGINNING;
    public static final String CONNECTED = "CONNECTED";

    String principalName;
    Flux<Tuple2<ActorRef, Object>> source;
    FluxSink<Object> sink;

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
        sink.next(message);
        if(message instanceof JoinMessage) joinedActors.add(from);
        else if(message instanceof UnjoinMessage) joinedActors.remove(from);
    }

    public void onNext(Tuple2<ActorRef, Object> tuple2) {
        ActorRef to = tuple2.getT1();
        Object message = tuple2.getT2();
        tell(to, message);
        if(message instanceof JoinMessage) joinedActors.add(to);
        else if(message instanceof UnjoinMessage) joinedActors.remove(to);
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

    public void notifyDisconnected(String reason) {
        Disconnected disconnected = new Disconnected(principalName, reason);
        tell(getContext().getParent(), disconnected);
        for(ActorRef actor : joinedActors) {
            tell(actor, disconnected);
        }
    }

}
