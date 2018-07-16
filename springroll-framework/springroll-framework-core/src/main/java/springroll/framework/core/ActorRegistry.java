package springroll.framework.core;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;

import java.util.List;

public interface ActorRegistry {

    void register(ActorRef ref);
    void unregister(ActorRef ref);

    ActorRef resovle(String path);

    List<ActorRef> resolveAll(String path);

    ActorSelection select(String path);

    List<ActorSelection> selectAll(String path);

}
