package springroll.example.chat;

import java.io.Serializable;

public class ChatterSaid implements Serializable {

    String chatterName;
    String content;

    public ChatterSaid() { }

    public ChatterSaid(String chatterName, String content) {
        this.chatterName = chatterName;
        this.content = content;
    }

    public String getChatterName() {
        return chatterName;
    }

    public void setChatterName(String chatterName) {
        this.chatterName = chatterName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
