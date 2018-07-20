package springroll.framework.core;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import springroll.framework.core.util.SimpleMultiValueMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CoordinatedActorRegistry implements ActorRegistry {
    private static Logger log = LoggerFactory.getLogger(CoordinatedActorRegistry.class);

    public class Registration {
        String actorPath;
        String actorClassName;
        Double loadFactor;
        ActorRef cachedActorRef;
        ActorSelection cachedActorSelection;

        public Registration(String actorPath, String actorClassName) {
            this.actorPath = actorPath;
            this.actorClassName = actorClassName;
        }

        public String getNamesapce() {
            int n = actorClassName.lastIndexOf(".");
            return n < 0 ? "" : actorClassName.substring(0, n);
        }

        ActorRef getActorRef() {
            if(cachedActorRef == null) cachedActorRef = springActorSystem.resolve(actorPath);
            return cachedActorRef;
        }

        ActorSelection getActorSelection() {
            if(cachedActorSelection == null) cachedActorSelection = springActorSystem.select(actorPath);
            return cachedActorSelection;
        }
    }

    SpringActorSystem springActorSystem;
    Coordinator coordinator;

    SimpleMultiValueMap<String, Registration> localActors = new SimpleMultiValueMap<>();
    SimpleMultiValueMap<String, Registration> remoteActors = new SimpleMultiValueMap<>();
    Function<List<Registration>, Registration> actorElectStrategy = registrations -> {
        Collections.sort(registrations, Comparator.comparing(registration -> registration.loadFactor));
        return registrations.get(0);
    };

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


    public synchronized void onProvide(String actorPath, String actorClassName) {
        String shortPath = ActorRegistry.shortPath(actorPath);
        remoteActors.add(shortPath, new Registration(actorPath, actorClassName));
    }

    public synchronized void onUnprovide(String actorPath) {
        remoteActors.findAndRemove((key, registration) -> registration.actorPath.equals(actorPath));
    }

    @Override
    public synchronized void register(ActorRef actorRef, Class<? extends Actor> actorClass) {
        String actorPath = actorRef.path().toString();
        String shortPath = ActorRegistry.shortPath(actorPath);
        localActors.add(shortPath, new Registration(actorPath, actorClass.getCanonicalName()));
        if(coordinator != null)
            coordinator.provide(springActorSystem.getServingRoot() + shortPath, actorClass.getCanonicalName());
    }

    @Override
    public synchronized void report(ActorRef actorRef, double loadFactor) {
    }

    @Override
    public synchronized void unregister(ActorRef ref) {
        String actorPath = ref.path().toString();
        String shortPath = ActorRegistry.shortPath(actorPath);
        localActors.findAndRemove((key, registration) -> registration.actorPath.equals(actorPath));
        if(coordinator != null) coordinator.unprovide(springActorSystem.getServingRoot() + shortPath);
    }

    @Override
    public synchronized ActorRef resovle(String path) {
        String shortPath = ActorRegistry.userPath(path);
        Registration registration = actorElectStrategy.apply(localActors.get(shortPath));
        if(registration != null) return registration.getActorRef();
        if(coordinator != null) {
            registration = actorElectStrategy.apply(remoteActors.get(shortPath));
            if(registration != null) return registration.getActorRef();
        }
        return null;
    }

    @Override
    public synchronized List<ActorRef> resolveAll(String path) {
        String shortPath = ActorRegistry.userPath(path);
        List<ActorRef> result = new ArrayList<>();
        result.addAll(localActors.get(shortPath).stream()
                .map(registration -> registration.getActorRef())
                .collect(Collectors.toList()));
        if(coordinator != null) {
            result.addAll(remoteActors.get(shortPath).stream()
                    .filter(registration -> registration.actorPath.indexOf(springActorSystem.getServingRoot()) == -1)
                    .map(registration -> registration.getActorRef())
                    .collect(Collectors.toList()));
        }
        return result;
    }

    @Override
    public synchronized ActorSelection select(String path) {
        String shortPath = ActorRegistry.userPath(path);
        Registration registration = actorElectStrategy.apply(localActors.get(shortPath));
        if(registration != null) return registration.getActorSelection();
        if(coordinator != null) {
            registration = actorElectStrategy.apply(remoteActors.get(shortPath));
            if(registration != null) return registration.getActorSelection();
        }
        return null;
    }

    @Override
    public synchronized List<ActorSelection> selectAll(String path) {
        String shortPath = ActorRegistry.userPath(path);
        List<ActorSelection> result = new ArrayList<>();
        result.addAll(localActors.get(shortPath).stream()
                .map(registration -> registration.getActorSelection())
                .collect(Collectors.toList()));
        if(coordinator != null) {
            result.addAll(remoteActors.get(shortPath).stream()
                    .filter(registration -> registration.actorPath.indexOf(springActorSystem.getServingRoot()) == -1)
                    .map(registration -> registration.getActorSelection())
                    .collect(Collectors.toList()));
        }
        return result;
    }

    @Override
    public String askNamespace(String path) {
        String shortPath = ActorRegistry.userPath(path);
        Registration registration = localActors.getFirst(shortPath);
        if(registration != null) return registration.getNamesapce();
        if(coordinator != null) {
            registration = remoteActors.getFirst(shortPath);
            if(registration != null) return registration.getNamesapce();
        }
        return null;
    }

}
