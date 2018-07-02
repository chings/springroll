package springroll.framework.core;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import springroll.framework.annotation.State;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class GenericActor extends AbstractActor {
    private static Logger log = LoggerFactory.getLogger(GenericActor.class);

    private static Map<Class<? extends GenericActor>, Map<String, Map<Class, Method>>> behaviorsCache = new HashMap<>();
    private synchronized static Map<String, Map<Class, Method>> analyseBehaviors(Class actorClass) {
        Map<String, Map<Class, Method>> result = behaviorsCache.get(actorClass);
        if(result != null) return result;
        result = new HashMap<>();
        for(Method method : actorClass.getMethods()) {
            String name = method.getName();
            if(!name.startsWith("on")) continue;
            Class<?>[] paramTypes = method.getParameterTypes();
            if(paramTypes.length != 1) continue;
            State state = method.getAnnotation(State.class);
            String stateKey = state != null ? state.value() : "";
            Map<Class, Method> stateBehaviors = result.get(stateKey);
            if(stateBehaviors == null) {
                stateBehaviors = new HashMap<>();
                result.put(stateKey, stateBehaviors);
            }
            if(!method.isAccessible()) method.setAccessible(true);
            stateBehaviors.put(paramTypes[0], method);
        }
        behaviorsCache.put(actorClass, result);
        return result;
    }

    Map<String, Map<Class, Method>> allStateBehaviors = analyseBehaviors(this.getClass());

    @Override
    public AbstractActor.Receive createReceive() {
        return stateReceive("");
    }

    public Receive stateReceive(String state) {
        Map<Class, Method> stateBehaviors = allStateBehaviors.get(state);
        if(stateBehaviors == null) {
            log.warn("empty stateBehaviors, should terminate");
            terminate();
            return null;
        }
        ReceiveBuilder receiveBuilder = receiveBuilder();
        for(Map.Entry<Class, Method> stateBehavior : stateBehaviors.entrySet()) {
            receiveBuilder.match(stateBehavior.getKey(), (arg) -> {
                Object result = stateBehavior.getValue().invoke(this, arg);
                if(Boolean.FALSE.equals(result)) terminate();
            });
        }
        return receiveBuilder.build();
    }

    protected void tell(ActorRef ref, Object message) {
        ref.tell(message, this.getSelf());
    }

    protected void become(String state) {
        this.getContext().become(stateReceive(state));
    }

    protected void terminate() {
        this.getContext().stop(this.getSelf());
    }

}
