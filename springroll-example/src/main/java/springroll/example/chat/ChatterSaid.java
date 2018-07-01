package springroll.example.chat;

import java.io.Serializable;

public class ChatterSaid implements Serializable {

    String chatterName;
    String content;

    public ChatterSaid(String chatterName, String content) {
        this.chatterName = chatterName;
        this.content = content;
    }

}
