package springroll.framework.core;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

public class Actors {

    public static final FiniteDuration INSTANTLY = FiniteDuration.create(100, TimeUnit.MICROSECONDS);
    public static final FiniteDuration SECONDLY = FiniteDuration.create(1, TimeUnit.SECONDS);
    public static final FiniteDuration MINUTElY = FiniteDuration.create(1, TimeUnit.MINUTES);

    public static void tell(ActorRef actor, Object message) {
        actor.tell(message, ActorRef.noSender());
    }

    public static void tell(ActorSelection actor, Object message) {
        actor.tell(message, ActorRef.noSender());
    }

    public static String shortPath(String actorPath) {
        int n = 0;
        for(int i = 0; i < 3; i++) {
            n = actorPath.indexOf('/', n + 1);
        }
        return actorPath.substring(n);
    }

    public static String shortPath(ActorRef actorRef) {
        return shortPath(actorRef.path().toString());
    }

}
