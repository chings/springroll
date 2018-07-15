package springroll.framework.core;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;

import java.util.List;

public interface ActorRegistry {

    void register(ActorRef ref);
    void unregister(ActorRef ref);

    ActorRef resovle(String shortPath);
    List<ActorRef> resolveAll(String shortPath);

    ActorSelection select(String shortPath);
    List<ActorSelection> selectAll(String shortPath);

}
