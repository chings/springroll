package springroll.framework.core;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;

public interface ActorRegistry {

    void register(ActorRef ref);
    void unregister(ActorRef ref);

    ActorRef get(String shortPath);
    ActorSelection select(String shortPath);

}
