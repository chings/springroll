package springroll.framework.core;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ActorRegistry {

    void register(ActorRef actorRef, String namespace);
    void update(ActorRef actorRef, double loadFactor);
    void unregister(ActorRef actorRef);

    ActorRef resovle(String pathOrName);
    List<ActorRef> resolveAll(String pathOrName);

    ActorSelection select(String pathOrName);
    List<ActorSelection> selectAll(String pathOrName);

    String namespace(String pathOrName);

    Pattern ACTOR_PATH_PATTERN = Pattern.compile("akka.*://([^/]+)(/.*)");

    static String path(String actorPath) {
        Matcher matcher = ACTOR_PATH_PATTERN.matcher(actorPath);
        if(!matcher.matches()) throw new IllegalArgumentException("bad format of ActorPath");
        return matcher.group(2);
    }

    static String path(ActorRef actorRef) {
        return path(actorRef.path().toString());
    }

    static String userPath(String pathOrName) {
        if(pathOrName.startsWith("/user/")) return pathOrName;
        return pathOrName.charAt(0) == '/' ? "/user" + pathOrName : "/user/" + pathOrName;
    }

}
