package springroll.framework.connector;

import akka.actor.ActorRef;

public class MessageDelivery {

    Object message;
    ActorRef to;

    public MessageDelivery(Object message, ActorRef to) {
        this.message = message;
        this.to = to;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public ActorRef getTo() {
        return to;
    }

    public void setTo(ActorRef to) {
        this.to = to;
    }

}
