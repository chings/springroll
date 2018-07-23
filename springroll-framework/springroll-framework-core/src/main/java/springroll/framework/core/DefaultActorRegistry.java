package springroll.framework.core;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultActorRegistry implements ActorRegistry {
    private static Logger log = LoggerFactory.getLogger(DefaultActorRegistry.class);

    public class Registration {

        String actorPath;
        String namespace;
        Double loadFactor = 0.0;

        ActorRef cachedActorRef;
        ActorSelection cachedActorSelection;

        Registration(String actorPath, Object... array) {
            this.actorPath = actorPath;
            fromArray(array);
        }

        void fromArray(Object[] array) {
            if(array.length > 0) namespace = array[0].toString();
            if(array.length > 1) try {
                loadFactor = Double.valueOf(array[1].toString());
            } catch(Exception x) {
                log.warn("Ugh! {}", x);
            }
        }

        public Object[] toArray() {
            return new Object[] { namespace, loadFactor };
        }

        public String getActorPath() {
            return actorPath;
        }

        public void setActorPath(String actorPath) {
            this.actorPath = actorPath;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public Double getLoadFactor() {
            return loadFactor;
        }

        public void setLoadFactor(Double loadFactor) {
            this.loadFactor = loadFactor;
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

    MultiValueMap<String, Registration> localActors = new LinkedMultiValueMap<>();
    MultiValueMap<String, Registration> remoteActors = new LinkedMultiValueMap<>();

    Function<List<Registration>, Registration> actorRouter = registrations -> {
        if(CollectionUtils.isEmpty(registrations)) return null;
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
        remoteActors.clear();
        coordinator.listenProvide(this::onProvide);
        coordinator.listenUnprovide(this::onUnprovide);
        coordinator.synchronize(this::onProvide);
    }

    public void setActorRouter(Function<List<Registration>, Registration> actorRouter) {
        this.actorRouter = actorRouter;
    }

    public synchronized void onProvide(String actorPath, Object[] data) {
        Registration registration = find(remoteActors, actorPath);
        if(registration != null) {
            registration.fromArray(data);
            return;
        }
        String path = ActorRegistry.path(actorPath);
        remoteActors.add(path, new Registration(actorPath, data));
    }

    public synchronized void onUnprovide(String actorPath) {
        remove(remoteActors, actorPath);
    }

    @Override
    public synchronized void register(ActorRef actorRef, String namespace) {
        String actorPath = actorRef.path().toString();
        String path = ActorRegistry.path(actorPath);
        Registration registration = new Registration(actorPath, namespace);
        localActors.add(path, registration);
        if(coordinator != null) {
            coordinator.provide(springActorSystem.getServingRoot() + path, registration.toArray());
        }
    }

    @Override
    public synchronized void update(ActorRef actorRef, double loadFactor) {
        String actorPath = actorRef.path().toString();
        Registration registration = find(localActors, actorPath);
        if(registration != null) return;
        registration.loadFactor = loadFactor;
        if(coordinator != null) {
            String path = ActorRegistry.path(actorPath);
            coordinator.provide(springActorSystem.getServingRoot() + path, registration.toArray());
        }
    }

    @Override
    public synchronized void unregister(ActorRef actorRef) {
        String actorPath = actorRef.path().toString();
        String path = ActorRegistry.path(actorPath);
        remove(localActors, actorPath);
        if(coordinator != null) {
            coordinator.unprovide(springActorSystem.getServingRoot() + path);
        }
    }

    @Override
    public synchronized ActorRef resovle(String pathOrName) {
        String path = ActorRegistry.userPath(pathOrName);
        Registration registration = actorRouter.apply(localActors.get(path));
        if(registration != null) return registration.getActorRef();
        if(coordinator != null) {
            registration = actorRouter.apply(remoteActors.get(path));
            if(registration != null) return registration.getActorRef();
        }
        return null;
    }

    @Override
    public synchronized List<ActorRef> resolveAll(String pathOrName) {
        String path = ActorRegistry.userPath(pathOrName);
        List<ActorRef> result = new ArrayList<>();
        result.addAll(localActors.get(path).stream()
                .map(registration -> registration.getActorRef())
                .collect(Collectors.toList()));
        if(coordinator != null) {
            result.addAll(remoteActors.get(path).stream()
                    .filter(registration -> registration.actorPath.indexOf(springActorSystem.getServingRoot()) == -1)
                    .map(registration -> registration.getActorRef())
                    .collect(Collectors.toList()));
        }
        return result;
    }

    @Override
    public synchronized ActorSelection select(String pathOrName) {
        String path = ActorRegistry.userPath(pathOrName);
        Registration registration = actorRouter.apply(localActors.get(path));
        if(registration != null) return registration.getActorSelection();
        if(coordinator != null) {
            registration = actorRouter.apply(remoteActors.get(path));
            if(registration != null) return registration.getActorSelection();
        }
        return null;
    }

    @Override
    public synchronized List<ActorSelection> selectAll(String pathOrName) {
        String path = ActorRegistry.userPath(pathOrName);
        List<ActorSelection> result = new ArrayList<>();
        result.addAll(localActors.get(path).stream()
                .map(registration -> registration.getActorSelection())
                .collect(Collectors.toList()));
        if(coordinator != null) {
            result.addAll(remoteActors.get(path).stream()
                    .filter(registration -> registration.actorPath.indexOf(springActorSystem.getServingRoot()) == -1)
                    .map(registration -> registration.getActorSelection())
                    .collect(Collectors.toList()));
        }
        return result;
    }

    @Override
    public String namespace(String pathOrName) {
        String path = ActorRegistry.userPath(pathOrName);
        Registration registration = localActors.getFirst(path);
        if(registration != null) return registration.namespace;
        if(coordinator != null) {
            registration = remoteActors.getFirst(path);
            if(registration != null) return registration.namespace;
        }
        return null;
}

    static Registration find(MultiValueMap<String, Registration> actorRegistrations, String actorPath) {
        String path = ActorRegistry.path(actorPath);
        List<Registration> registrations = actorRegistrations.get(path);
        if(registrations == null) return null;
        for(Registration registration : registrations) {
            if(registration.actorPath.equals(actorPath)) {
                return registration;
            }
        }
        return null;
    }
    
    static void remove(MultiValueMap<String, Registration> actorRegistrations, String actorPath) {
        String path = ActorRegistry.path(actorPath);
        List<Registration> registrations = actorRegistrations.get(path);
        if(registrations == null) return;
        registrations.removeIf(registration -> registration.actorPath.equals(actorPath));
    }

}
