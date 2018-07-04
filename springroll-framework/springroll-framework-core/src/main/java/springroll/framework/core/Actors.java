package springroll.framework.core;

import akka.actor.*;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

public class Actors {
    public static final FiniteDuration INSTANTLY = FiniteDuration.create(100, TimeUnit.MICROSECONDS);
    public static final FiniteDuration SECONDLY = FiniteDuration.create(1, TimeUnit.SECONDS);
    public static final FiniteDuration MINUTElY = FiniteDuration.create(1, TimeUnit.MINUTES);

    public static ActorRef spawn(ActorSystem system, Class<? extends Actor> actorClass, Object... args) {
        return system.actorOf(Props.create(actorClass, args));
    }

    public static ActorRef spawn(ActorContext context, Class<? extends Actor> actorClass, Object... args) {
        return context.actorOf(Props.create(actorClass, args));
    }

    public static void tell(ActorRef actor, Object message) {
        actor.tell(message, ActorRef.noSender());
    }

    public static void tell(ActorSelection actor, Object message) {
        actor.tell(message, ActorRef.noSender());
    }

}
