package springroll.example.chat;

import akka.actor.ActorRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import springroll.framework.core.GenericActor;

import java.util.HashMap;
import java.util.Map;

public class Chat extends GenericActor {
    private static Logger log = LoggerFactory.getLogger(Chat.class);

    Map<ActorRef, String> chatters = new HashMap<>();

    public void on(Join join, ActorRef from) {
        if(chatters.containsValue(join.chatterName)) {
            tell(from, new NotJoined("name '" + join.chatterName + "' has been taken."));
            return;
        }
        chatters.put(from, join.chatterName);
        chatters.forEach((to, name) -> {
            if(name.equals(join.chatterName)) {
                tell(to, new Joined(chatters.values()));
            } else {
                tell(to, new ChatterJoined(join.chatterName));
            }
        });
    }

    public void on(Say say, ActorRef from) {
        if(!chatters.containsKey(from)) {
            log.warn("'{}', a unjoined chatter try to say: {}", say.content);
            return;
        }
        String senderName = chatters.get(from);
        chatters.forEach((to, name) -> {
            tell(to, new ChatterSaid(senderName, say.content));
        });
    }

    public void on(Leave leave, ActorRef from) {
        String senderName = chatters.remove(from);
        chatters.forEach((to, name) -> {
            tell(to, new ChatterLeft(senderName));
        });
    }

}