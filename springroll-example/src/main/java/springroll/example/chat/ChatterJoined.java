package springroll.example.chat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChatterJoined implements Serializable {

    String newChatterName;
    List<String> currentChatterNames;

    public ChatterJoined(String newChatterName) {
        this.newChatterName = newChatterName;
    }

    public void setCurrentChatters(Collection<String> chatterNames) {
        currentChatterNames = new ArrayList<>();
        currentChatterNames.addAll(chatterNames);
    }

}
