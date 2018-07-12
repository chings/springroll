package springroll.framework.connector.protocol;

import akka.actor.ActorRef;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.util.function.Tuple2;

import java.io.Serializable;

public class Connected implements Serializable {

    String principal;
    Flux<Tuple2<ActorRef, Object>> source;
    FluxSink<Object> sink;

    public Connected() { }

    public Connected(String principal, Flux<Tuple2<ActorRef, Object>> source, FluxSink<Object> sink) {
        this.principal = principal;
        this.source = source;
        this.sink = sink;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public Flux<Tuple2<ActorRef, Object>> getSource() {
        return source;
    }

    public void setSource(Flux<Tuple2<ActorRef, Object>> source) {
        this.source = source;
    }

    public FluxSink<Object> getSink() {
        return sink;
    }

    public void setSink(FluxSink<Object> sink) {
        this.sink = sink;
    }

}
