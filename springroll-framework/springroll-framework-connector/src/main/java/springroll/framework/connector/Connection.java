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

public class Connection extends GenericActor {
    private static Logger log = LoggerFactory.getLogger(Connection.class);

    public static String CONNECTING = At.BEGINNING;
    public static String CONNECTED = "CONNECTED";

    String principal;
    Flux<Tuple2<Object, ActorRef>> source;
    FluxSink<Object> sink;

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

    public void onNext(Tuple2<Object, ActorRef> tuple2) {
        tell(tuple2.getT2(), tuple2.getT1());
    }

    public void onError(Throwable error) {
        log.error("Ugh! {}", error.getMessage(), error);
        onComplete();
    }

    public void onComplete() {
        tell(getContext().getParent(), new Disconnected(principal));
        terminate();
    }

}
