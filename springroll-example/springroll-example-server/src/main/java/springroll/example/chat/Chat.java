package springroll.example.chat;

import akka.actor.ActorRef;
import springroll.framework.core.GenericActor;

import java.util.HashMap;
import java.util.Map;

public class Chat extends GenericActor {

    Map<ActorRef, String> chatters = new HashMap<>();

    public void on(Join join, ActorRef from) {
        if(chatters.containsKey(join.senderName)) return;
        chatters.put(from, join.senderName);
        chatters.forEach((to, name) -> {
            ChatterJoined message = new ChatterJoined(join.senderName);
            if(name.equals(join.senderName)) message.setCurrentChatterNames(chatters.values());
            tell(to, message);
        });
    }

    public void on(Say say) {
        if(!chatters.containsKey(say.senderName)) return;
        chatters.forEach((to, name) -> {
            tell(to, new ChatterSaid(say.senderName, say.content));
        });
    }

    public void on(Leave leave) {
        if(!chatters.containsKey(leave.senderName)) return;
        chatters.remove(leave.senderName);
        chatters.forEach((to, name) -> {
            tell(to, new ChatterLeft(leave.senderName));
        });
    }

}