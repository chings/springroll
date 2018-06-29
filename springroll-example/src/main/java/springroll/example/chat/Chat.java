package springroll.example.chat;

import akka.typed.ActorRef;

import java.util.HashMap;
import java.util.Map;

public class Chat {

    String name;
    Map<String, ActorRef<ChatResponse>> chatters = new HashMap<>();

    public void on(Join request) {
        if(chatters.containsKey(request.name)) return;
        chatters.put(request.name, request.from);
        request.from.tell(new Joined(chatters.keySet()));
        chatters.forEach((key, value) -> {
            if(!key.equals(request.name)) value.tell(new SomeoneJoined(request.name));
        });
    }

    public void on(Say request) {
        if(!chatters.containsKey(request.name)) return;
        chatters.forEach((key, value) -> { value.tell(new Said(request.name, request.message)); });
    }

    public void on(Leave request) {
        if(!chatters.containsKey(request.name)) return;
        chatters.remove(request.name);
        chatters.forEach((key, value) -> { value.tell(new SomeoneLeft(request.name)); });
    }

}