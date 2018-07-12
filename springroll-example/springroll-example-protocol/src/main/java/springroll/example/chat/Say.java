package springroll.example.chat;

import java.io.Serializable;

public class Say implements Serializable {

    String senderName;
    String content;

    public Say() { }

    public Say(String content) {
        this.content = content;
    }

    public Say(String senderName, String content) {
        this.senderName = senderName;
        this.content = content;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}