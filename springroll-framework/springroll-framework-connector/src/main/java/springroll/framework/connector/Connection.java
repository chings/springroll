package springroll.framework.connector;

import akka.actor.ActorRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import springroll.framework.connector.protocol.Connected;
import springroll.framework.core.AgentActor;
import springroll.framework.core.GenericActor;
import springroll.framework.core.annotation.At;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Connection extends GenericActor {
    private static Logger log = LoggerFactory.getLogger(Connection.class);

    public static PathMatcher pathMatcher = new AntPathMatcher() ;

    public static String CONNECTING = At.BEGINNING;
    public static String CONNECTED = "CONNECTED";

    Flux<NormalizedMessage> source;
    FluxSink<Object> sink;
    Map<String, ActorRef> agentsByMapping = new HashMap<>();

    public void on(Connected connected) {
        Map<Class<? extends AgentActor>, List<String>> agentRegistrations = connected.getAgentRegistrations();
        for(Map.Entry<Class<? extends AgentActor>, List<String>> entry : agentRegistrations.entrySet()) {
            Class<? extends AgentActor> agentClass = entry.getKey();
            ActorRef agent = spawn(agentClass, sink);
            for(String mapping : entry.getValue()) {
                agentsByMapping.put(mapping, agent);
            }
        }
        source.subscribe(this::onNext, this::onError, this::onComplete);
        become(CONNECTED);
    }

    public ActorRef match(String to) {
        ActorRef actor = agentsByMapping.get(to);
        if(actor != null) return actor;
        for(Map.Entry<String, ActorRef> entry : agentsByMapping.entrySet()) {
            String mapping = entry.getKey();
            if(pathMatcher.match(mapping, to)) return actor;
        }
        return null;
    }

    public void onNext(NormalizedMessage message) {
        String to = message.getTo();
        ActorRef agent = match(to);
        if(agent == null) return;
        tell(agent, message);
    }

    public void onError(Throwable error) {
        log.error("Ugh! {}", error.getMessage(), error);
    }

    public void onComplete() {

    }

}
