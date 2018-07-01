package springroll.example.chat;

import java.io.Serializable;

public class Leave implements Serializable {

    String doerName;

    public Leave(String doerName) {
        this.doerName = doerName;
    }

}
