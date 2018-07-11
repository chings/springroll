package springroll.framework.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import springroll.framework.connector.protocol.Connected;
import springroll.framework.core.GenericActor;
import springroll.framework.core.annotation.At;

public class Connection extends GenericActor {
    private static Logger log = LoggerFactory.getLogger(Connection.class);

    public static String CONNECTING = At.BEGINNING;
    public static String CONNECTED = "CONNECTED";

    String principal;
    Flux<MessageDelivery> source;
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

    public void onNext(MessageDelivery delivery) {
        tell(delivery.getTo(), delivery.getMessage());
    }

    public void onError(Throwable error) {
        log.error("Ugh! {}", error.getMessage(), error);
        onComplete();
    }

    public void onComplete() {
        //TODO: cleanup
    }

}
