package springroll.framework.core;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import springroll.framework.core.util.SimpleMultiValueMap;

import java.util.List;
import java.util.function.Function;

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

    protected SimpleMultiValueMap<String, Registration> localActors = new SimpleMultiValueMap<>();
    protected Function<List<Registration>, Integer> elector = registrations -> 0;

    public void setElector(Function<List<Registration>, Integer> elector) {
        this.elector = elector;
    }

    public static String shortPath(String actorPath) {
        int n = 0;
        for(int i = 0; i < 3; i++) {
            n = actorPath.indexOf('/', n + 1);
        }
        return actorPath.substring(n);
    }

    public synchronized void register(ActorRef ref) {
        String actorPath = ref.path().toString();
        String shortPath = shortPath(actorPath);
        localActors.add(shortPath, new Registration(actorPath, ref));
    }

    public synchronized void unregister(ActorRef ref) {
        localActors.forEachValue((key, registration) -> registration.actorPath.equals(ref.path().toString()));
    }

    public synchronized ActorRef get(String shortPath) {
        Registration registration = localActors.getOne(shortPath, elector);
        return registration != null ? registration.getActorRef() : null;
    }

    public synchronized ActorSelection select(String shortPath) {
        Registration registration = localActors.getOne(shortPath, elector);
        return registration != null ? registration.getActorSelection() : null;
    }

}
