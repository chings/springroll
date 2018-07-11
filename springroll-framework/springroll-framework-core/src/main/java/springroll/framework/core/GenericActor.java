package springroll.framework.core;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import springroll.framework.core.annotation.At;
import springroll.framework.core.annotation.On;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class GenericActor extends AbstractActor {
    private static Logger log = LoggerFactory.getLogger(GenericActor.class);

    private static Map<Class<? extends GenericActor>, Map<String, Map<Class<?>, Method>>> behaviorsCache = new HashMap<>();
    private static Map<String, Map<Class<?>, Method>> analyseBehaviors(Class<? extends GenericActor> actorClass) {
        Map<String, Map<Class<?>, Method>> result = behaviorsCache.get(actorClass);
        if(result != null) return result;
        result = new HashMap<>();
        for(Method method : actorClass.getMethods()) {
            On on = method.getAnnotation(On.class);
            if(on == null && !method.getName().startsWith("on")) continue;
            Class<?>[] paramTypes = method.getParameterTypes();
            if(paramTypes.length != 1) {
                log.debug("'{}' skipped, an 'on' method must have only 1 param.", method.toGenericString());
                continue;
            }
            Class<?> paramType = on != null && on.value() != Object.class ? on.value() : paramTypes[0];
            At at = method.getAnnotation(At.class);
            String[] stateKeys = at != null ? at.value() : new String[] { At.BEGINNING };
            for(String stateKey : stateKeys) {
                Map<Class<?>, Method> stateBehaviors = result.computeIfAbsent(stateKey, k -> new HashMap<>());
                if(!method.isAccessible()) method.setAccessible(true);
                stateBehaviors.put(paramType, method);
            }
        }
        behaviorsCache.put(actorClass, result);
        return result;
    }

    protected Map<String, Map<Class<?>, Method>> allStateBehaviors = analyseBehaviors(this.getClass());

    @Override
    public AbstractActor.Receive createReceive() {
        return stateReceive(At.BEGINNING);
    }

    protected Receive stateReceive(String state) {
        Map<Class<?>, Method> stateBehaviors = new HashMap<>();
        Map<Class<?>, Method> behaviors = allStateBehaviors.get(At.ALL);
        if(behaviors != null) stateBehaviors.putAll(behaviors);
        behaviors = allStateBehaviors.get(state);
        if(behaviors != null) stateBehaviors.putAll(behaviors);
        if(stateBehaviors.isEmpty()) {
            log.warn("empty stateBehaviors, should terminate");
            terminate();
            return null;
        }
        ReceiveBuilder receiveBuilder = receiveBuilder();
        for(Map.Entry<Class<?>, Method> stateBehavior : stateBehaviors.entrySet()) {
            receiveBuilder.match(stateBehavior.getKey(), arg -> {
                Object result = stateBehavior.getValue().invoke(this, arg);
                if(result == null) return;
                if(result instanceof String) {
                    become((String)result);
                    return;
                }
                if(Boolean.FALSE.equals(result)) terminate();
            });
        }
        receiveBuilder.matchAny(arg -> log.warn("unrecognized: {}", arg));
        return receiveBuilder.build();
    }

    protected ActorRef spawn(Class<? extends Actor> childActorClass, Object... args) {
        return this.getContext().actorOf(Props.create(childActorClass, args), childActorClass.getSimpleName());
    }

    protected void tell(ActorRef actor, Object message) {
        actor.tell(message, this.getSelf());
    }

    public void tell(ActorSelection actor, Object message) {
        actor.tell(message, this.getSelf());
    }

    protected void become(String state) {
        this.getContext().become(stateReceive(state));
    }

    protected void terminate() {
        this.getContext().stop(this.getSelf());
    }

}
