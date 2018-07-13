package springroll.framework.core;

import akka.actor.*;
import akka.dispatch.sysmsg.Terminate;
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
    private synchronized static Map<String, Map<Class<?>, Method>> analyseBehaviors(Class<? extends GenericActor> actorClass) {
        return behaviorsCache.computeIfAbsent(actorClass, actorClazz -> {
            Map<String, Map<Class<?>, Method>> result = new HashMap<>();
            for(Method method : actorClazz.getMethods()) {
                On on = method.getAnnotation(On.class);
                if(on == null && !method.getName().startsWith("on")) continue;
                Class<?> messageType = null;
                if(on != null) messageType = on.value();
                if(messageType == null || messageType == Object.class) {
                    Class<?>[] paramTypes = method.getParameterTypes();
                    for(Class<?> paramType : paramTypes) {
                        if(paramType.isAssignableFrom(ActorRef.class)) continue;
                        messageType = paramType;
                        break;
                    }
                }
                if(messageType == null) {
                    log.warn("can not map for {}, just skipped.", method);
                    continue;
                }
                At at = method.getAnnotation(At.class);
                String[] stateKeys = at != null ? at.value() : new String[] { At.BEGINNING };
                for(String stateKey : stateKeys) {
                    Map<Class<?>, Method> stateBehaviors = result.computeIfAbsent(stateKey, key -> new HashMap<>());
                    if(!method.isAccessible()) method.setAccessible(true);
                    stateBehaviors.put(messageType, method);
                }
            }
            return result;
        });
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
        ReceiveBuilder receiveBuilder = receiveBuilder();
        for(Map.Entry<Class<?>, Method> stateBehavior : stateBehaviors.entrySet()) {
            receiveBuilder.match(stateBehavior.getKey(), message -> {
                Method method = stateBehavior.getValue();
                Object[] args = preHandle(message, method);
                Object result = method.invoke(this, args);
                postHandle(result);
            });
        }
        receiveBuilder.matchAny(this::otherwise);
        return receiveBuilder.build();
    }

    protected Object[] preHandle(Object message, Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] result = new Object[paramTypes.length];
        for(int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            if(paramType.isAssignableFrom(message.getClass())) {
                result[i] = message;
                continue;
            }
            if(paramType.isAssignableFrom(ActorRef.class)) {
                result[i] = getSender();
                continue;
            }
            result[i] = null;
        }
        return result;
    }

    protected void postHandle(Object result) {
        if(result == null) return;
        if(result instanceof String) {
            become((String)result);
            return;
        }
        if(result instanceof Terminate) {
            terminate();
            return;
        }
    }

    public void otherwise(Object message) {
        log.warn("unhandled: {}", message);
    }

    protected ActorRef spawn(String name, Class<? extends Actor> childActorClass, Object... args) {
        return this.getContext().actorOf(Props.create(childActorClass, args), name);
    }

    protected ActorRef spawn(Class<? extends Actor> childActorClass, Object... args) {
        return spawn(childActorClass.getSimpleName(), childActorClass, args);
    }

    protected void tell(ActorRef actor, Object message) {
        actor.tell(message, this.getSelf());
    }

    protected void tell(ActorSelection actor, Object message) {
        actor.tell(message, this.getSelf());
    }

    protected void become(String state) {
        this.getContext().become(stateReceive(state));
    }

    protected void terminate() {
        this.getContext().stop(this.getSelf());
    }

}
