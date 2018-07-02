package springroll.framework;

import akka.actor.*;

public class Actors {

    public static ActorRef spawn(ActorSystem system, Class<? extends AbstractActor> actorClass, Object... args) {
        return system.actorOf(Props.create(actorClass, args));
    }

    public static ActorRef spawn(ActorContext context, Class<? extends AbstractActor> actorClass, Object... args) {
        return context.actorOf(Props.create(actorClass, args));
    }

    public static void tell(ActorRef ref, Object message) {
        ref.tell(message, ActorRef.noSender());
    }

}
