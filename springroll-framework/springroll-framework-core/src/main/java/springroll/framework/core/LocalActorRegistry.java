package springroll.framework.core;

import akka.actor.ActorPath;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;

import java.util.HashMap;
import java.util.Map;

public class LocalActorRegistry implements ActorRegistry {

    ActorSystem actorSystem;

    public LocalActorRegistry(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    class Registration {
        String actorPath;
        ActorRef cachedActorRef;
        ActorSelection cachedActorSelection;
        public Registration(String actorPath) {
            this.actorPath = actorPath;
        }
        public Registration(String actorPath, ActorRef cachedActorRef) {
            this.cachedActorRef = cachedActorRef;
        }
        @SuppressWarnings("deprecated")
        public ActorRef getActorRef() {
            if(cachedActorRef == null) cachedActorRef = actorSystem.actorFor(actorPath);
            return cachedActorRef;
        }
        public ActorSelection getActorSelection() {
            if(cachedActorSelection == null) cachedActorSelection = actorSystem.actorSelection(actorPath);
            return cachedActorSelection;
        }
    }

    Map<String, Registration> localActors = new HashMap<>();

    static String host(ActorPath actorPath) {
        String rootPath = actorPath.root().toString();
        return rootPath.substring(0, rootPath.length() - 1);
    }

    static String shortPath(ActorPath actorPath) {
        return actorPath.toString().substring(host(actorPath).length());
    }

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
