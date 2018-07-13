package springroll.example.chat;

import springroll.framework.protocol.JoinMessage;

import java.io.Serializable;

public class Join implements JoinMessage, Serializable {

    String senderName;

    public Join() { }

    public Join(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

}
