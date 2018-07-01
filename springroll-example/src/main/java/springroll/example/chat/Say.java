package springroll.example.chat;

import java.io.Serializable;

public class Say implements Serializable {

    String doerName;
    String content;

    public Say(String doerName, String content) {
        this.doerName = doerName;
        this.content = content;
    }

}
