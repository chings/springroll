package springroll.example.chat;

import java.io.Serializable;

public class ChatterJoined implements Serializable {

    String chatterName;

    public ChatterJoined() { }

    public ChatterJoined(String chatterName) {
        this.chatterName = chatterName;
    }

    public String getChatterName() {
        return chatterName;
    }

    public void setChatterName(String chatterName) {
        this.chatterName = chatterName;
    }

}
