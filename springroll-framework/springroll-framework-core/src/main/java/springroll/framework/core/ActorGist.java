package springroll.framework.core;

import akka.actor.*;
import akka.dispatch.sysmsg.Terminate;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import scala.compat.java8.FutureConverters;
import springroll.framework.core.annotation.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class ActorGist extends AbstractActor {
    private static Logger log = LoggerFactory.getLogger(ActorGist.class);
    private static Map<Class<? extends ActorGist>, Map<String, Map<Class<?>, Method>>> behaviorsCache = new HashMap<>();
    private static final String INITIIAL = "";
    private static final String ANY = "*";

    private static Map<String, Map<Class<?>, Method>> analyseBehaviors(Class<?> stateClass, Map<String, Map<Class<?>, Method>> result) {
        Map<Class<?>, Method> stateBehaviors = new LinkedHashMap<>();
        for(Method method : stateClass.getMethods()) {
            if(method.getAnnotation(NotOn.class) != null) continue;
            On on = method.getAnnotation(On.class);
            if(on == null && !method.getName().startsWith("on")) continue;
            Class<?> messageType = null;
            if(on != null) messageType = on.value();
            if(messageType == null || messageType == Object.class) {
                Class<?>[] paramTypes = method.getParameterTypes();
                for(Class<?> paramType : paramTypes) {
                    if(ActorRef.class.isAssignableFrom(paramType)) continue;
                    messageType = paramType;
                    break;
                }
            }
            if(messageType == null) {
                log.warn("can not map for {}, just skipped.", method);
                continue;
            }
            if(!method.isAccessible()) method.setAccessible(true);
            stateBehaviors.put(messageType, method);
        }

        if(!stateBehaviors.isEmpty()) {
            State state = stateClass.getAnnotation(State.class);
            Set<String> stateKeys = new HashSet<>();
            stateKeys.add(stateClass.getCanonicalName());
            if(stateClass.getAnnotation(Initial.class) != null) stateKeys.add(INITIIAL);
            if(stateClass.getAnnotation(Any.class) != null) stateKeys.add(ANY);
            if(state != null) for(String stateName : state.value()) {
                if(StringUtils.hasText(stateName)) stateKeys.add(stateName);
            }
            for(String stateKey : stateKeys) {
                result.put(stateKey, stateBehaviors);
            }
        }

        for(Class<?> innerClass : stateClass.getClasses()) {
            int modifiers = innerClass.getModifiers();
            if(innerClass.getAnnotation(State.class) == null) continue;
            if(Modifier.toString(modifiers).contains("static")) continue;
            analyseBehaviors(innerClass, result);
        }
        return result;
    }

    private synchronized static Map<String, Map<Class<?>, Method>> analyseBehaviors(Class<? extends ActorGist> actorClass) {
        return behaviorsCache.computeIfAbsent(actorClass, actorClazz -> {
            Map<String, Map<Class<?>, Method>> result = analyseBehaviors(actorClazz, new LinkedHashMap<String, Map<Class<?>, Method>>());
            if(!result.containsKey(INITIIAL)) {
                for(Map<Class<?>, Method> value : result.values()) {
                    result.put(INITIIAL, value);
                    break;
                }
            }
            return result;
        });
    }

    private static Map<Class<?>, Object> createStateObjects(Object stateObject, Map<Class<?>, Object> result) {
        for(Class<?> innerStateClass : stateObject.getClass().getClasses()) {
            int modifiers = innerStateClass.getModifiers();
            if(innerStateClass.getAnnotation(State.class) == null) continue;
            if(Modifier.toString(modifiers).contains("static")) continue;
            try {
                Constructor<?> constructor = innerStateClass.getConstructors()[0];
                Object innerStateObject = constructor.newInstance(stateObject);
                result.put(innerStateClass, innerStateObject);
                createStateObjects(innerStateObject, result);
            } catch(InstantiationException | IllegalAccessException | InvocationTargetException x) {
                log.error("inner state object instantiated failed: {}", x.getMessage(), x);
                throw new RuntimeException(x);
            }
        }
        return result;
    }

    private static Map<Class<?>, Object> createStateObjects(Object stateObject) {
        Map<Class<?>, Object> result = new HashMap<>();
        result.put(stateObject.getClass(), stateObject);
        return createStateObjects(stateObject, result);
    }

    protected Map<String, Map<Class<?>, Method>> allStateBehaviors = analyseBehaviors(this.getClass());
    protected Map<Class<?>, Object> allStateObjects = createStateObjects(this);
    protected String currentState = INITIIAL;

    @Override
    public AbstractActor.Receive createReceive() {
        return stateReceive(currentState);
    }

    protected Receive stateReceive(String state) {
        Map<Class<?>, Method> stateBehaviors = new HashMap<>();
        Map<Class<?>, Method> behaviors = allStateBehaviors.get(ANY);
        if(behaviors != null) stateBehaviors.putAll(behaviors);
        behaviors = allStateBehaviors.get(state);
        if(behaviors != null) stateBehaviors.putAll(behaviors);
        ReceiveBuilder receiveBuilder = receiveBuilder();
        for(Map.Entry<Class<?>, Method> stateBehavior : stateBehaviors.entrySet()) {
            receiveBuilder.match(stateBehavior.getKey(), message -> {
                Method method = stateBehavior.getValue();
                Object target = allStateObjects.get(method.getDeclaringClass());
                Object[] args = preHandle(message, method);
                Object result = method.invoke(target, args);
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
        if(result instanceof Class<?>) {
            become((Class<?>)result);
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

    public ActorRef spawn(String name, Props props) {
        return getContext().actorOf(props, name);
    }

    public ActorRef spawn(Props props) {
        return getContext().actorOf(props);
    }

    public ActorRef spawn(String name, Class<? extends Actor> childActorClass, Object... args) {
        return getContext().actorOf(Props.create(childActorClass, args), name);
    }

    public ActorRef spawn(Class<? extends Actor> childActorClass, Object... args) {
        return getContext().actorOf(Props.create(childActorClass, args), childActorClass.getSimpleName());
    }

    public void become(String state) {
        currentState = state;
        getContext().become(stateReceive(currentState));
    }

    public void become(Class<?> stateClass) {
        become(stateClass.getCanonicalName());
    }

    public void tell(ActorRef actor, Object message) {
        actor.tell(message, getSelf());
    }

    public void tell(ActorSelection actor, Object message) {
        actor.tell(message, getSelf());
    }

    public void ask(ActorRef actor, Object message, Consumer<Object> consumer) {
        FutureConverters.toJava(Patterns.ask(actor, message, Timeout.apply(1, TimeUnit.SECONDS)))
                .handle((reply, error) -> {
                    consumer.accept(reply != null ? reply : error);
                    return null;
                });
    }

    public void ask(ActorSelection actor, Object message, Consumer<Object> consumer) {
        FutureConverters.toJava(Patterns.ask(actor, message, Timeout.apply(1, TimeUnit.SECONDS)))
                .handle((reply, error) -> {
                    consumer.accept(reply != null ? reply : error);
                    return null;
                });
    }

    public void reply(Object message) {
        getSender().tell(message, getSelf());
    }

    public void terminate() {
        getContext().stop(getSelf());
    }

}
