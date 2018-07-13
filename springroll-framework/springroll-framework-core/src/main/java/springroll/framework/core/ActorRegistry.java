package springroll.framework.core;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;

import java.util.List;

public interface ActorRegistry {

    void register(ActorRef ref);
    void unregister(ActorRef ref);

    ActorRef get(String shortPath);

    List<ActorRef> getAll(String shortPath);

    ActorSelection select(String shortPath);

    List<ActorSelection> selectAll(String shortPath);

}
