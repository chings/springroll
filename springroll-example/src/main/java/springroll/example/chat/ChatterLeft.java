package springroll.example.chat;

import java.io.Serializable;

public class ChatterLeft implements Serializable {

    String chatterName;

    public ChatterLeft(String chatterName) {
        this.chatterName = chatterName;
    }

}
