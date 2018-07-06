package springroll.framework.core;

import akka.actor.ActorPath;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;

import java.util.HashMap;
import java.util.Map;

public class LocalActorRegistry implements ActorRegistry {

    protected ActorSystem actorSystem;

    public LocalActorRegistry(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    public class Registration {
        String actorPath;
        ActorRef cachedActorRef;
        ActorSelection cachedActorSelection;
        public Registration(String actorPath) {
            this.actorPath = actorPath;
        }
        public Registration(String actorPath, ActorRef cachedActorRef) {
            this.actorPath = actorPath;
            this.cachedActorRef = cachedActorRef;
        }
        public ActorRef getActorRef() {
            if(cachedActorRef == null) cachedActorRef = Actors.resolve(actorSystem, actorPath);
            return cachedActorRef;
        }
        public ActorSelection getActorSelection() {
            if(cachedActorSelection == null) cachedActorSelection = actorSystem.actorSelection(actorPath);
            return cachedActorSelection;
        }
    }

    public static String host(ActorPath actorPath) {
        String rootPath = actorPath.root().toString();
        return rootPath.substring(0, rootPath.length() - 1);
    }

    public static String shortPath(ActorPath actorPath) {
        return actorPath.toString().substring(host(actorPath).length());
    }

    protected Map<String, Registration> localActors = new HashMap<>();

    public synchronized void register(ActorRef ref) {
        ActorPath actorPath = ref.path();
        String shortPath = shortPath(actorPath);
        localActors.put(shortPath, new Registration(actorPath.toString(), ref));
    }

    public synchronized void unregister(ActorRef ref) {
        ActorPath actorPath = ref.path();
        String shortPath = shortPath(actorPath);
        localActors.remove(shortPath);
    }

    public synchronized ActorRef get(String shortPath) {
        Registration registration = localActors.get(shortPath);
        return registration != null ? registration.getActorRef() : null;
    }

    public synchronized ActorSelection select(String shortPath) {
        Registration registration = localActors.get(shortPath);
        return registration != null ? registration.getActorSelection() : null;
    }

}
