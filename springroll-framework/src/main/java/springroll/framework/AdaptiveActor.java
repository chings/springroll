package springroll.framework;

import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;

public class AdaptiveActor extends AbstractActorWithTimers {

    @Override
    public Receive createReceive() {
        return null;
    }

    protected void notify(ActorRef ref, Object message) {
        ref.tell(message, this.getSelf());
    }

}
