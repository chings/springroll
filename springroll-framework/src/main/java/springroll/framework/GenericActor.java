package springroll.framework;

import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
import springroll.framework.annotation.State;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class GenericActor extends AbstractActorWithTimers {

    Map<String, Map<Class, Method>> allStateBehaviors = analyseBehaviors(this.getClass());

    @Override
    public Receive createReceive() {
        return stateReceive("");
    }

    public Receive stateReceive(String state) {
        Map<Class, Method> stateBehaviors = allStateBehaviors.get(state);
        if(stateBehaviors == null) {
            //TODO: Terminate Self
        }
        ReceiveBuilder receiveBuilder = receiveBuilder();
        for(Map.Entry<Class, Method> stateBehavior : stateBehaviors.entrySet()) {
            receiveBuilder.match(stateBehavior.getKey(), (arg) -> {
                Object result = stateBehavior.getValue().invoke(this, arg);
                if(Boolean.FALSE.equals(result)) {
                    //TODO: Terminate Self
                }
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
            State stateAnno = method.getAnnotation(State.class);
            String stateKey = stateAnno != null ? stateAnno.value() : null;
            Map<Class, Method> stateBehaviors = result.get(stateKey);
            if(stateBehaviors == null) {
                stateBehaviors = new HashMap<>();
                result.put(stateKey, stateBehaviors);
            }
            stateBehaviors.put(paramTypes[0], method);
        }
        behaviorsCache.put(actorClass, result);
        return result;
    }

}
