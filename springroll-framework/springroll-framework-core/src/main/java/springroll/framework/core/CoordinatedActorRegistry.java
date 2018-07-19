package springroll.framework.core;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import springroll.framework.core.util.SimpleMultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static springroll.framework.core.Actors.shortPath;
import static springroll.framework.core.Actors.userPath;

public class CoordinatedActorRegistry implements ActorRegistry {
    private static Logger log = LoggerFactory.getLogger(CoordinatedActorRegistry.class);

    public class Registration {
        String actorPath;
        String actorClassName;
        ActorRef cachedActorRef;
        ActorSelection cachedActorSelection;

        public Registration(String actorPath, String actorClassName) {
            this.actorPath = actorPath;
            this.actorClassName = actorClassName;
        }
        public Registration(String actorPath, ActorRef cachedActorRef) {
            this.actorPath = actorPath;
            this.cachedActorRef = cachedActorRef;
        }

        public String getNamesapce() {
            int n = actorClassName.lastIndexOf(".");
            return n < 0 ? "" : actorClassName.substring(0, n);
        }
        public ActorRef getActorRef() {
            if(cachedActorRef == null) cachedActorRef = springActorSystem.resolve(actorPath);
            return cachedActorRef;
        }
        public ActorSelection getActorSelection() {
            if(cachedActorSelection == null) cachedActorSelection = springActorSystem.select(actorPath);
            return cachedActorSelection;
        }
    }

    SpringActorSystem springActorSystem;
    Coordinator coordinator;

    SimpleMultiValueMap<String, Registration> localActors = new SimpleMultiValueMap<>();
    Function<List<Registration>, Integer> localElector = registrations -> 0;

    SimpleMultiValueMap<String, Registration> remoteActors = new SimpleMultiValueMap<>();
    Function<List<Registration>, Integer> remoteElector = registrations -> springActorSystem.getRemoteRootPath().hashCode() % registrations.size();

    @Autowired
    public void setSpringActorSystem(SpringActorSystem springActorSystem) {
        this.springActorSystem = springActorSystem;
    }

    @Autowired(required = false)
    public void setCoordinator(Coordinator coordinator) {
        this.coordinator = coordinator;

        coordinator.listenProvide(this::onProvide);
        coordinator.listenUnprovide(this::onUnprovide);

        remoteActors.clear();
        coordinator.synchronize(this::onProvide);
    }

    public void setLocalElector(Function<List<Registration>, Integer> localElector) {
        this.localElector = localElector;
    }

    public void setRemoteElector(Function<List<Registration>, Integer> remoteElector) {
        this.remoteElector = remoteElector;
    }

    public synchronized void onProvide(String actorPath, String actorClassName) {
        String shortPath = shortPath(actorPath);
        remoteActors.add(shortPath, new Registration(actorPath, actorClassName));
    }

    public synchronized void onUnprovide(String actorPath) {
        remoteActors.findAndRemove((key, registration) -> registration.actorPath.equals(actorPath));
    }

    @Override
    public synchronized void register(ActorRef actorRef, Class<? extends Actor> actorClass) {
        String actorPath = actorRef.path().toString();
        String shortPath = shortPath(actorPath);
        localActors.add(shortPath, new Registration(actorPath, actorClass.getCanonicalName()));
        if(coordinator != null)
            coordinator.provide(springActorSystem.getRemoteRootPath() + shortPath, actorClass.getCanonicalName());
    }

    @Override
    public synchronized void unregister(ActorRef ref) {
        String actorPath = ref.path().toString();
        String shortPath = shortPath(actorPath);
        localActors.findAndRemove((key, registration) -> registration.actorPath.equals(actorPath));
        if(coordinator != null) coordinator.unprovide(springActorSystem.getRemoteRootPath() + shortPath);
    }

    @Override
    public synchronized ActorRef resovle(String path) {
        String shortPath = userPath(path);
        Registration registration = localActors.getOne(shortPath, localElector);
        if(registration != null) return registration.getActorRef();
        if(coordinator != null) {
            registration = remoteActors.getOne(shortPath, remoteElector);
            if(registration != null) return registration.getActorRef();
        }
        return null;
    }

    @Override
    public synchronized List<ActorRef> resolveAll(String path) {
        String shortPath = userPath(path);
        List<ActorRef> result = new ArrayList<>();
        result.addAll(localActors.get(shortPath).stream()
                .map(registration -> registration.getActorRef())
                .collect(Collectors.toList()));
        if(coordinator != null) {
            result.addAll(remoteActors.get(shortPath).stream()
                    .filter(registration -> registration.actorPath.indexOf(springActorSystem.getRemoteRootPath()) == -1)
                    .map(registration -> registration.getActorRef())
                    .collect(Collectors.toList()));
        }
        return result;
    }

    @Override
    public synchronized ActorSelection select(String path) {
        String shortPath = userPath(path);
        Registration registration = localActors.getOne(shortPath, localElector);
        if(registration != null) return registration.getActorSelection();
        if(coordinator != null) {
            registration = remoteActors.getOne(shortPath, remoteElector);
            if(registration != null) return registration.getActorSelection();
        }
        return null;
    }

    @Override
    public synchronized List<ActorSelection> selectAll(String path) {
        String shortPath = userPath(path);
        List<ActorSelection> result = new ArrayList<>();
        result.addAll(localActors.get(shortPath).stream()
                .map(registration -> registration.getActorSelection())
                .collect(Collectors.toList()));
        if(coordinator != null) {
            result.addAll(remoteActors.get(shortPath).stream()
                    .filter(registration -> registration.actorPath.indexOf(springActorSystem.getRemoteRootPath()) == -1)
                    .map(registration -> registration.getActorSelection())
                    .collect(Collectors.toList()));
        }
        return result;
    }

    @Override
    public String askNamespace(String path) {
        String shortPath = userPath(path);
        Registration registration = localActors.getOne(shortPath, localElector);
        if(registration != null) return registration.getNamesapce();
        if(coordinator != null) {
            registration = remoteActors.getOne(shortPath, remoteElector);
            if(registration != null) return registration.getNamesapce();
        }
        return null;
    }

}
