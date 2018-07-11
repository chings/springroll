package springroll.framework.core;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import springroll.framework.core.util.SimpleMultiValueMap;

import java.util.List;
import java.util.function.Function;

public class CoordinatedActorRegistry implements ActorRegistry {
    private static Logger log = LoggerFactory.getLogger(CoordinatedActorRegistry.class);

    public static class Registration {
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
        public ActorRef getActorRef(ActorSystem actorSystem) {
            if(cachedActorRef == null) cachedActorRef = Actors.resolve(actorSystem, actorPath);
            return cachedActorRef;
        }
        public ActorSelection getActorSelection(ActorSystem actorSystem) {
            if(cachedActorSelection == null) cachedActorSelection = actorSystem.actorSelection(actorPath);
            return cachedActorSelection;
        }
    }

    public static String shortPath(String actorPath) {
        int n = 0;
        for(int i = 0; i < 3; i++) {
            n = actorPath.indexOf('/', n + 1);
        }
        return actorPath.substring(n);
    }

    ActorSystem actorSystem;
    String myHost;
    Coordinator coordinator;

    SimpleMultiValueMap<String, Registration> localActors = new SimpleMultiValueMap<>();
    Function<List<Registration>, Integer> localElector = registrations -> 0;

    SimpleMultiValueMap<String, Registration> remoteActors = new SimpleMultiValueMap<>();
    Function<List<Registration>, Integer> remoteElector = registrations -> myHost.hashCode() % registrations.size();

    @Autowired
    public void setActorSystem(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
        myHost = Actors.getHost(actorSystem);
    }

    @Autowired(required = false)
    public void setCoordinator(Coordinator coordinator) {
        this.coordinator = coordinator;
        coordinator.listenProvide(this::onProvide);
        coordinator.listenUnprovide(this::onUnprovide);
        coordinator.synchronize(this::onSynchronize);
    }

    public void setLocalElector(Function<List<Registration>, Integer> localElector) {
        this.localElector = localElector;
    }

    public void setRemoteElector(Function<List<Registration>, Integer> remoteElector) {
        this.remoteElector = remoteElector;
    }

    public synchronized void onSynchronize(List<String> all) {
        remoteActors.clear();
        for(String actorPath : all) {
            String shortPath = shortPath(actorPath);
            remoteActors.add(shortPath, new Registration(actorPath));
        }
    }

    public synchronized void onProvide(String actorPath) {
        String shortPath = shortPath(actorPath);
        remoteActors.add(shortPath, new Registration(actorPath));
    }

    public synchronized void onUnprovide(String actorPath) {
        remoteActors.findAndRemove((key, registration) -> registration.actorPath.equals(actorPath));
    }

    @Override
    public synchronized void register(ActorRef ref) {
        String actorPath = ref.path().toString();
        String shortPath = shortPath(actorPath);
        localActors.add(shortPath, new Registration(actorPath, ref));
        if(coordinator != null) coordinator.provide(myHost + shortPath);
    }

    @Override
    public synchronized void unregister(ActorRef ref) {
        String actorPath = ref.path().toString();
        String shortPath = shortPath(actorPath);
        localActors.findAndRemove((key, registration) -> registration.actorPath.equals(actorPath));
        if(coordinator != null) coordinator.unprovide(myHost + shortPath);
    }

    @Override
    public synchronized ActorRef get(String shortPath) {
        Registration registration = localActors.getOne(shortPath, localElector);
        if(registration != null) return registration.getActorRef(actorSystem);
        if(coordinator != null) {
            registration = remoteActors.getOne(shortPath, remoteElector);
            if(registration != null) return registration.getActorRef(actorSystem);
        }
        return null;
    }

    @Override
    public synchronized ActorSelection select(String shortPath) {
        Registration registration = localActors.getOne(shortPath, localElector);
        if(registration != null) return registration.getActorSelection(actorSystem);
        if(coordinator != null) {
            registration = remoteActors.getOne(shortPath, remoteElector);
            if(registration != null) return registration.getActorSelection(actorSystem);
        }
        return null;
    }

}
