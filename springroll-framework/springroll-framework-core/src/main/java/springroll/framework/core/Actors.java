package springroll.framework.core;

import akka.actor.*;
import akka.serialization.Serialization;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

public class Actors {

    public static final FiniteDuration INSTANTLY = FiniteDuration.create(100, TimeUnit.MICROSECONDS);
    public static final FiniteDuration SECONDLY = FiniteDuration.create(1, TimeUnit.SECONDS);
    public static final FiniteDuration MINUTElY = FiniteDuration.create(1, TimeUnit.MINUTES);

    public static ActorRef spawn(ActorSystem system, Class<? extends Actor> actorClass, Object... args) {
        return system.actorOf(Props.create(actorClass, args), actorClass.getSimpleName());
    }

    public static ActorRef spawn(ActorContext context, Class<? extends Actor> actorClass, Object... args) {
        return context.actorOf(Props.create(actorClass, args), actorClass.getSimpleName());
    }

    public static ActorRef resolve(ActorSystem system, String actorPath) {
        return ((ExtendedActorSystem)system).provider().resolveActorRef(actorPath);
    }

    @SuppressWarnings("deprecated")
    public static String getHost(ActorSystem system) {
        String rootPath = Serialization.serializedActorPath(system.actorFor("/"));
        return rootPath.substring(0, rootPath.length() - 1);
    }

    public static void tell(ActorRef actor, Object message) {
        actor.tell(message, ActorRef.noSender());
    }

    public static void tell(ActorSelection actor, Object message) {
        actor.tell(message, ActorRef.noSender());
    }

}
