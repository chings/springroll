package springroll.framework.core;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import org.springframework.beans.factory.DisposableBean;
import springroll.framework.core.util.SimpleMultiValueMap;

import java.util.List;

public class CoordinatedActorRegistry extends LocalActorRegistry implements DisposableBean {

    protected Coordinator coordinator;
    protected String myHost;

    public CoordinatedActorRegistry(ActorSystem actorSystem) {
        super(actorSystem);
        myHost = Actors.getHost(actorSystem);
        elector = list -> myHost.hashCode() % list.size();
    }

    public CoordinatedActorRegistry(ActorSystem actorSystem, Coordinator coordinator) {
        this(actorSystem);
        setCoordinator(coordinator);
    }

    protected SimpleMultiValueMap<String, Registration> remoteActors = new SimpleMultiValueMap<>();

    public synchronized void onSynchronize(List<String> all) {
        remoteActors.clear();
        for(String actorPath : all) {
            String[] tuple = split(actorPath);
            String host = tuple[0], shortPath = tuple[1];
            remoteActors.add(shortPath, new Registration(host + shortPath));
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
        String shortPath = shortPath(actorPath);
        remoteActors.add(shortPath, new Registration(actorPath));
    }

    public synchronized void onUnprovide(String actorPath) {
        remoteActors.forEachValue((key, registration) -> registration.actorPath.equals(actorPath));
    }

    public void setCoordinator(Coordinator coordinator) {
        this.coordinator = coordinator;
        coordinator.synchronize(this::onSynchronize);
        coordinator.listenProvide(this::onProvide);
        coordinator.listenUnprovide(this::onUnprovide);
    }

    @Override
    public synchronized void register(ActorRef ref) {
        super.register(ref);
        if(coordinator != null) coordinator.provide(myHost + shortPath(ref.path().toString()));
    }

    @Override
    public synchronized void unregister(ActorRef ref) {
        super.unregister(ref);
        if(coordinator != null) coordinator.unprovide(myHost + shortPath(ref.path().toString()));
    }


    @Override
    public synchronized ActorRef get(String shortPath) {
        ActorRef result = super.get(shortPath);
        if(result != null) return result;
        if(coordinator != null) {
            Registration registration = remoteActors.getOne(shortPath, elector);
            if(registration != null) return registration.getActorRef();
        }
        return null;
    }

    @Override
    public synchronized ActorSelection select(String shortPath) {
        ActorSelection result = super.select(shortPath);
        if(result != null) return result;
        if(coordinator != null) {
            Registration registration = remoteActors.getOne(shortPath, elector);
            if(registration != null) return registration.getActorSelection();
        }
        return null;
    }

    @Override
    public void destroy() {
        if(coordinator != null) coordinator.unprovide();
    }

}
