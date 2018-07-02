package springroll.example.chat;

import java.io.Serializable;

public class Leave implements Serializable {

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
