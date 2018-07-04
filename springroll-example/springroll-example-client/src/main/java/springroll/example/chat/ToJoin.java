package springroll.example.chat;

import akka.actor.ActorSelection;

import java.io.Serializable;

public class ToJoin implements Serializable {

    ActorSelection chat;
    String name;

    public ToJoin() { }

    public ToJoin(ActorSelection chat, String name) {
        this.chat = chat;
        this.name = name;
    }

    public ActorSelection getChat() {
        return chat;
    }

    public void setChat(ActorSelection chat) {
        this.chat = chat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
