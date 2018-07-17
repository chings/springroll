package springroll.example.chat;

import java.io.Serializable;
import java.util.Collection;

public class Joined implements Serializable {

    Collection<String> allChatterNames;

    public Joined() { }

    public Joined(Collection<String> allChatterNames) {
        this.allChatterNames = allChatterNames;
    }

    public Collection<String> getAllChatterNames() {
        return allChatterNames;
    }

    public void setAllChatterNames(Collection<String> allChatterNames) {
        this.allChatterNames = allChatterNames;
    }

}
