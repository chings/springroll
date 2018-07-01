package springroll.example.chat;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;

import java.util.HashMap;
import java.util.Map;

public class ChatActor0 extends AbstractActor {

    Map<String, ActorRef> chatters = new HashMap<>();

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(Join.class, this::on)
                .match(Say.class, this::on)
                .match(Leave.class, this::on)
                .build();
    }

    public void on(Join join) {
        if(chatters.containsKey(join.doerName)) return;
        chatters.put(join.doerName, join.from);
        chatters.forEach((name, ref) -> {
            ChatterJoined msg = new ChatterJoined(join.doerName);
            if(name.equals(join.doerName)) msg.setCurrentChatters(chatters.keySet());
            ref.tell(msg, this.getSelf());
        });
    }

    public void on(Say say) {
        if(!chatters.containsKey(say.doerName)) return;
        chatters.forEach((name, ref) -> {
            ref.tell(new ChatterSaid(say.doerName, say.content), this.getSelf());
        });
    }

    public void on(Leave leave) {
        if(!chatters.containsKey(leave.doerName)) return;
        chatters.remove(leave.doerName);
        chatters.forEach((name, ref) -> {
            ref.tell(new ChatterLeft(leave.doerName), this.getSelf());
        });
    }

}