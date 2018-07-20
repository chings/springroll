package springroll.framework.core;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ActorRegistry {

    void register(ActorRef actorRef, Class<? extends Actor> actorClass);
    void report(ActorRef actorRef, double loadFactor);
    void unregister(ActorRef actorRef);

    ActorRef resovle(String path);
    List<ActorRef> resolveAll(String path);

    ActorSelection select(String path);
    List<ActorSelection> selectAll(String path);

    String askNamespace(String path);

    Pattern actorPathPattern = Pattern.compile("akka.*://([^/]+)(/.*)");

    static String uriPath(String path) {
        Matcher matcher = actorPathPattern.matcher(path);
        if(!matcher.matches()) throw new IllegalArgumentException("bad format of ActorPath");
        return matcher.group(2);
    }

    static String uriPath(ActorRef actorRef) {
        return uriPath(actorRef.path().toString());
    }

    static String userPath(String path) {
        if(path.startsWith("/user/")) return path;
        return path.charAt(0) == '/' ? "/user" + path : "/user/" + path;
    }

}
