package springroll.example.chat;

import java.io.Serializable;

public class Join implements Serializable {

    String chatterName;

    public Join() { }

    public Join(String chatterName) {
        this.chatterName = chatterName;
    }

    public String getChatterName() {
        return chatterName;
    }

    public void setChatterName(String chatterName) {
        this.chatterName = chatterName;
    }

}
