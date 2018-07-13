package springroll.framework.connector.protocol;

import akka.actor.ActorRef;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.util.function.Tuple2;

import java.io.Serializable;

public class Connected implements Serializable {

    String principalName;
    Flux<Tuple2<ActorRef, Object>> source;
    FluxSink<Object> sink;

    public Connected() { }

    public Connected(String principalName, Flux<Tuple2<ActorRef, Object>> source, FluxSink<Object> sink) {
        this.principalName = principalName;
        this.source = source;
        this.sink = sink;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
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
