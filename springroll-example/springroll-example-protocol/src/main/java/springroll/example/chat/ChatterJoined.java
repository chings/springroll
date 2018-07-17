package springroll.example.chat;

import java.io.Serializable;
import java.util.Collection;

public class ChatterJoined implements Serializable {

    String chatterName;
    Collection<String> allChatterNames;

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

    public Collection<String> getAllChatterNames() {
        return allChatterNames;
    }

    public void setAllChatterNames(Collection<String> allChatterNames) {
        this.allChatterNames = allChatterNames;
    }

}
