package springroll.framework.connector.protocol;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import springroll.framework.connector.NormalizedMessage;
import springroll.framework.core.AgentActor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Connected implements Serializable {

    String principal;
    Flux<NormalizedMessage> source;
    FluxSink<Object> sink;
    Map<Class<? extends AgentActor>, List<String>> agentRegistrations;

    public Connected() { }

    public Connected(String principal, Flux<NormalizedMessage> source, FluxSink<Object> sink, Map<Class<? extends AgentActor>, List<String>> agentRegistrations) {
        this.principal = principal;
        this.source = source;
        this.sink = sink;
        this.agentRegistrations = agentRegistrations;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public Flux<NormalizedMessage> getSource() {
        return source;
    }

    public void setSource(Flux<NormalizedMessage> source) {
        this.source = source;
    }

    public FluxSink<Object> getSink() {
        return sink;
    }

    public void setSink(FluxSink<Object> sink) {
        this.sink = sink;
    }

    public Map<Class<? extends AgentActor>, List<String>> getAgentRegistrations() {
        return agentRegistrations;
    }

    public void setAgentRegistrations(Map<Class<? extends AgentActor>, List<String>> agentRegistrations) {
        this.agentRegistrations = agentRegistrations;
    }

}
