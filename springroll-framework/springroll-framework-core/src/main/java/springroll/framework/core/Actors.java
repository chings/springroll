package springroll.framework.core;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /**
     * akka://my-sys/user/service-a/worker1                     (purely local)
     * akka.tcp://my-sys@host.example.com:5678/user/service-b   (remote)
     */
    public static Pattern actorPathPattern = Pattern.compile("akka.*://([^/]+)(/.*)");

    public static String shortPath(String path) {
        Matcher matcher = actorPathPattern.matcher(path);
        if(!matcher.matches()) throw new IllegalArgumentException("bad format of ActorPath");
        return matcher.group(2);
    }

    public static String shortPath(ActorRef actorRef) {
        return shortPath(actorRef.path().toString());
    }

    public static String userPath(String path) {
        if(path.startsWith("/user/")) return path;
        return path.charAt(0) == '/' ? "/user" + path : "/user/" + path;
    }

}
