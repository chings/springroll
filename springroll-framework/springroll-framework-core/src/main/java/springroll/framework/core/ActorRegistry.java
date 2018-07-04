package springroll.framework.core;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.util.HashMap;
import java.util.Map;

public class ActorRegistry {

    ActorSystem actorSystem;

    Map<String, ActorRef> localRefsByPath = new HashMap<>();

    public ActorRegistry(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    public synchronized void register(ActorRef ref) {
        localRefsByPath.put(ref.path().toString(), ref);
        //TODO: expose to coordinator
    }

    public ActorRef find(String actorPath) {
        return localRefsByPath.get(actorPath);
    }

}
