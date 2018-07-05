package springroll.example.chat;

import akka.actor.ActorRef;

import java.io.Serializable;

public class ToJoin implements Serializable {

    ActorRef chat;
    String name;

    public ToJoin() { }

    public ToJoin(ActorRef chat, String name) {
        this.chat = chat;
        this.name = name;
    }

    public ActorRef getChat() {
        return chat;
    }

    public void setChat(ActorRef chat) {
        this.chat = chat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
