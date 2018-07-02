package springroll.example.chat;

import akka.actor.ActorRef;
import springroll.framework.GenericActor;

import java.util.HashMap;
import java.util.Map;

public class ChatActor extends GenericActor {

    Map<String, ActorRef> chatters = new HashMap<>();

    public void on(Join join) {
        if(chatters.containsKey(join.senderName)) return;
        chatters.put(join.senderName, join.from);
        chatters.forEach((name, ref) -> {
            ChatterJoined message = new ChatterJoined(join.senderName);
            if(message.equals(join.senderName)) message.setCurrentChatterNames(chatters.keySet());
            tell(ref, message);
        });
    }

    public void on(Say say) {
        if(!chatters.containsKey(say.senderName)) return;
        chatters.forEach((name, ref) -> {
            tell(ref, new ChatterSaid(say.senderName, say.content));
        });
    }

    public void on(Leave leave) {
        if(!chatters.containsKey(leave.senderName)) return;
        chatters.remove(leave.senderName);
        chatters.forEach((name, ref) -> {
            tell(ref, new ChatterLeft(leave.senderName));
        });
    }

}