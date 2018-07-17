package springroll.example.chat;

import java.io.Serializable;

public class Say implements Serializable {

    String content;

    public Say() { }

    public Say(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}