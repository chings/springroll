package springroll.example.chat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChatterJoined implements Serializable {

    String newChatterName;
    List<String> currentChatterNames;

    public ChatterJoined() { }

    public ChatterJoined(String newChatterName) {
        this.newChatterName = newChatterName;
    }

    public String getNewChatterName() {
        return newChatterName;
    }

    public void setNewChatterName(String newChatterName) {
        this.newChatterName = newChatterName;
    }

    public List<String> getCurrentChatterNames() {
        return currentChatterNames;
    }

    public void setCurrentChatterNames(Collection<String> chatterNames) {
        if(currentChatterNames == null) currentChatterNames = new ArrayList<>();
        else currentChatterNames.clear();
        currentChatterNames.addAll(chatterNames);
    }

}
