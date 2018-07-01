package springroll.example.chat;


import akka.actor.ActorRef;

import java.io.Serializable;

public class Join implements Serializable {

    String doerName;
    ActorRef from;

    public Join(String doerName, ActorRef from) {
        this.doerName = doerName;
        this.from = from;
    }

}
