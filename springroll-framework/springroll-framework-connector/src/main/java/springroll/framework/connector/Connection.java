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

import java.util.LinkedHashMap;
import java.util.Map;

public class Connection extends GenericActor {
    private static Logger log = LoggerFactory.getLogger(Connection.class);

    public static String CONNECTING = At.BEGINNING;
    public static String CONNECTED = "CONNECTED";

    String principal;
    Flux<Tuple2<ActorRef, Object>> source;
    FluxSink<Object> sink;

    LinkedHashMap<ActorRef, Long> recentAssociations = new LinkedHashMap<ActorRef, Long>(10) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<ActorRef, Long> eldest) {
            return super.removeEldestEntry(eldest);
        }
    };

    public void on(Connected connected) {
        principal = connected.getPrincipal();
        source = connected.getSource();
        sink = connected.getSink();
        source.subscribe(this::onNext, this::onError, this::onComplete);
        become(CONNECTED);
    }

    @Override
    public void otherwise(Object message) {
        sink.next(message);
    }

    public void onNext(Tuple2<ActorRef, Object> tuple2) {
        ActorRef actorRef = tuple2.getT1();
        Object message = tuple2.getT2();
        tell(actorRef, message);
        recentAssociations.put(actorRef, System.currentTimeMillis());
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
        Disconnected disconnected = new Disconnected(principal, reason);
        tell(getContext().getParent(), disconnected);
        for(ActorRef actor : recentAssociations.keySet()) {
            tell(actor, disconnected);
        }
    }

}
