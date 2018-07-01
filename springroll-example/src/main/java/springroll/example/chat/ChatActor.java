package springroll.example.chat;

import akka.actor.ActorRef;
import springroll.framework.AdaptiveActor;
import springroll.framework.annotation.ActorComponent;

import java.util.HashMap;
import java.util.Map;

@ActorComponent("chat")
public class ChatActor extends AdaptiveActor {

    Map<String, ActorRef> chatters = new HashMap<>();

    public void on(Join join) {
        if(chatters.containsKey(join.doerName)) return;
        chatters.put(join.doerName, join.from);
        chatters.forEach((name, ref) -> {
            ChatterJoined message = new ChatterJoined(join.doerName);
            if(message.equals(join.doerName)) message.setCurrentChatters(chatters.keySet());
            notify(ref, message);
        });
    }

    public void on(Say say) {
        if(!chatters.containsKey(say.doerName)) return;
        chatters.forEach((name, ref) -> {
            notify(ref, new ChatterSaid(say.doerName, say.content));
        });
    }

    public void on(Leave leave) {
        if(!chatters.containsKey(leave.doerName)) return;
        chatters.remove(leave.doerName);
        chatters.forEach((name, ref) -> {
            notify(ref, new ChatterLeft(leave.doerName));
        });
    }

}