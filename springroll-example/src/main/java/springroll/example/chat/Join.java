package springroll.example.chat;

import akka.typed.ActorRef;

public class Join implements ChatRequest {

    String name;
    ActorRef<ChatResponse> from;

    public Join(String name, ActorRef<ChatResponse> from) {
        this.name = name;
        this.from = from;
    }

}
