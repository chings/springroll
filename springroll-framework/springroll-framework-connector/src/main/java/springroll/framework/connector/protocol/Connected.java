package springroll.framework.connector.protocol;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import springroll.framework.connector.Frame;

import java.io.Serializable;

public class Connected implements Serializable {

    String principalName;
    Flux<Frame> source;
    FluxSink<Frame> sink;

    public Connected() { }

    public Connected(String principalName, Flux<Frame> source, FluxSink<Frame> sink) {
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

    public Flux<Frame> getSource() {
        return source;
    }

    public void setSource(Flux<Frame> source) {
        this.source = source;
    }

    public FluxSink<Frame> getSink() {
        return sink;
    }

    public void setSink(FluxSink<Frame> sink) {
        this.sink = sink;
    }

}
