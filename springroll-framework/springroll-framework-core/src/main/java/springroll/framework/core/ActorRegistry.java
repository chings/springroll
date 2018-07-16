package springroll.framework.core;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;

import java.util.List;

public interface ActorRegistry {

    void register(ActorRef actorRef, Class<? extends Actor> actorClass);

    void unregister(ActorRef actorRef);

    ActorRef resovle(String path);
    List<ActorRef> resolveAll(String path);

    ActorSelection select(String path);
    List<ActorSelection> selectAll(String path);

    String askNamespace(String path);

}
