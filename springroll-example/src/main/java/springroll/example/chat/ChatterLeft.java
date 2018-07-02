package springroll.example.chat;

import java.io.Serializable;

public class ChatterLeft implements Serializable {

    String chatterName;

    public ChatterLeft() { }

    public ChatterLeft(String chatterName) {
        this.chatterName = chatterName;
    }

    public String getChatterName() {
        return chatterName;
    }

    public void setChatterName(String chatterName) {
        this.chatterName = chatterName;
    }

}
