package springroll.example.chat;

import springroll.framework.protocol.UnjoinMessage;

import java.io.Serializable;

public class Leave implements UnjoinMessage, Serializable {

    String senderName;

    public Leave() { }

    public Leave(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

}
