package springroll.example.chat;

import akka.actor.ActorRef;

import java.io.Serializable;

public class Join implements Serializable {

    String senderName;
    ActorRef from;

    public Join() { }

    public Join(String senderName, ActorRef from) {
        this.senderName = senderName;
        this.from = from;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public ActorRef getFrom() {
        return from;
    }

    public void setFrom(ActorRef from) {
        this.from = from;
    }

}
