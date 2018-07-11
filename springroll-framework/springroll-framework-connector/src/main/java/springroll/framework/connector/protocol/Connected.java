package springroll.framework.connector.protocol;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import springroll.framework.connector.MessageDelivery;

import java.io.Serializable;

public class Connected implements Serializable {

    String principal;
    Flux<MessageDelivery> source;
    FluxSink<Object> sink;

    public Connected() { }

    public Connected(String principal, Flux<MessageDelivery> source, FluxSink<Object> sink) {
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

    public Flux<MessageDelivery> getSource() {
        return source;
    }

    public void setSource(Flux<MessageDelivery> source) {
        this.source = source;
    }

    public FluxSink<Object> getSink() {
        return sink;
    }

    public void setSink(FluxSink<Object> sink) {
        this.sink = sink;
    }

}
