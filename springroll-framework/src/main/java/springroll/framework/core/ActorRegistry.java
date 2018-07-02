package springroll.framework.core;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.util.HashMap;
import java.util.Map;

public class ActorRegistry {

    ActorSystem actorSystem;

    Map<Class<? extends Actor>, ActorRef> refsByClass = new HashMap<>();

    public ActorRegistry(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    public synchronized void register(ActorRef ref, Class<? extends Actor> actorClass, String name) {
        refsByClass.put(actorClass, ref);
        //TODO: expose to coordinator
    }

    public synchronized ActorRef findByClass(Class<?> actorClass) {
        return refsByClass.get(actorClass);
    }

}
