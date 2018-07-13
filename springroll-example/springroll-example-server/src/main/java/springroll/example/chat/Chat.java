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
        if(chatters.containsValue(join.senderName)) {
            tell(from, new NotJoined("name '" + join.senderName + "' has been taken."));
            return;
        }
        chatters.put(from, join.senderName);
        chatters.forEach((to, name) -> {
            ChatterJoined message = new ChatterJoined(join.senderName);
            if(name.equals(join.senderName)) message.setCurrentChatterNames(chatters.values());
            tell(to, message);
        });
    }

    public void on(Say say, ActorRef from) {
        if(!chatters.containsKey(from)) {
            log.warn("'{}', a unjoined chatter try to say: {}", say.senderName, say.content);
            return;
        }
        chatters.forEach((to, name) -> {
            tell(to, new ChatterSaid(say.senderName, say.content));
        });
    }

    public void on(Leave leave, ActorRef from) {
        chatters.remove(from);
        chatters.forEach((to, name) -> {
            tell(to, new ChatterLeft(leave.senderName));
        });
    }

}