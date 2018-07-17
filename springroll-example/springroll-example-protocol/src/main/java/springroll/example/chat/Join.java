package springroll.example.chat;

import springroll.framework.protocol.JoinMessage;

import java.io.Serializable;

public class Join implements JoinMessage, Serializable {

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
