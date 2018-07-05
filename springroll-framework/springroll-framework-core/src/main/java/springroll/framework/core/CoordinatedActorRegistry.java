package springroll.framework.core;

import akka.actor.ActorPath;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.serialization.Serialization;
import org.springframework.beans.factory.DisposableBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CoordinatedActorRegistry extends LocalActorRegistry implements DisposableBean {

    String myHost;

    public CoordinatedActorRegistry(ActorSystem actorSystem) {
        super(actorSystem);
        String rootPath = Serialization.serializedActorPath(actorSystem.actorFor("/"));
        myHost = rootPath.substring(0, rootPath.length() - 1);
    }

    Map<String, Map<String, Registration>> remoteActors = null;

    public synchronized void onSynchronize(Map<String, List<String>> all) {
        remoteActors = new HashMap<>();
        for(Map.Entry<String, List<String>> entry : all.entrySet()) {
            String host = entry.getKey();
            Map<String, Registration> registrations = new HashMap<>();
            for(String shortPath : entry.getValue()) {
                Registration registration = new Registration(host + shortPath);
                registrations.put(shortPath, registration);
            }
            remoteActors.put(host, registrations);
        }
    }

    public static String[] split(String fullActorPath) {
        int n = 0;
        for(int i = 0; i < 3; i++) {
            n = fullActorPath.indexOf('/', n + 1);
        }
        return new String[] { fullActorPath.substring(0, n), fullActorPath.substring(n) };
    }

    public synchronized void onProvide(String actorPath) {
        String[] tuple = split(actorPath);
        String host = tuple[0], shortPath = tuple[1];
        Map<String, Registration> registrations = remoteActors.get(host);
        if(registrations == null) {
            registrations = new HashMap<>();
            remoteActors.put(host, registrations);
        }
        registrations.put(shortPath, new Registration(actorPath));
    }

    public synchronized void onUnProvide(String actorPath) {
        String[] tuple = split(actorPath);
        String host = tuple[0], shortPath = tuple[1];
        Map<String, Registration> registrations = remoteActors.get(host);
        if(registrations != null) {
            registrations.remove(shortPath);
            if(registrations.isEmpty()) {
                remoteActors.remove(host);
            }
        }
    }

    public synchronized void onUnProvideAll(String host) {
        remoteActors.remove(host);
    }

    Coordinator coordinator;

    public void setCoordinator(Coordinator coordinator) {
        this.coordinator = coordinator;
        coordinator.synchronize(this::onSynchronize);
        coordinator.listenProvide(this::onProvide);
        coordinator.listenUnProvide(this::onUnProvide);
        coordinator.listenUnProvideAll(this::onUnProvideAll);
    }

    public CoordinatedActorRegistry(ActorSystem actorSystem, Coordinator coordinator) {
        this(actorSystem);
        setCoordinator(coordinator);
    }

    @Override
    public synchronized void register(ActorRef ref) {
        ActorPath actorPath = ref.path();
        String shortPath = shortPath(actorPath);
        localActors.put(shortPath, new Registration(actorPath.toString(), ref));
        if(coordinator != null) coordinator.provide(myHost + shortPath);
    }

    @Override
    public synchronized void unregister(ActorRef ref) {
        ActorPath actorPath = ref.path();
        String shortPath = shortPath(actorPath);
        localActors.remove(shortPath);
        if(coordinator != null) coordinator.unprovide(myHost + shortPath);
    }

    Registration lookup(String shortPath, Function<List<Registration>, Integer> elector) {
        List<Registration> candidates = new ArrayList<>();
        for(Map.Entry<String, Map<String, Registration>> entry : remoteActors.entrySet()) {
            String host = entry.getKey();
            for (Map.Entry<String, Registration> entry1 : entry.getValue().entrySet()) {
                String shortPath1 = entry1.getKey();
                if(shortPath.equals(shortPath1)) candidates.add(entry1.getValue());
            }
        }
        return candidates.isEmpty() ? null : candidates.get(elector.apply(candidates));
    }

    Function<List<Registration>, Integer> defaultElector = list -> myHost.hashCode() % list.size();

    @Override
    public synchronized ActorRef get(String shortPath) {
        Registration registration = localActors.get(shortPath);
        if(registration != null) return registration.getActorRef();
        if(coordinator != null) {
            registration = lookup(shortPath, defaultElector);
            if (registration != null) return registration.getActorRef();
        }
        return null;
    }

    @Override
    public synchronized ActorSelection select(String shortPath) {
        Registration registration = localActors.get(shortPath);
        if(registration != null) return registration.getActorSelection();
        if(coordinator != null) {
            registration = lookup(shortPath, defaultElector);
            if (registration != null) return registration.getActorSelection();
        }
        return null;
    }

    @Override
    public void destroy() {
        if(coordinator != null) coordinator.unprovideAll(myHost);
    }

}
