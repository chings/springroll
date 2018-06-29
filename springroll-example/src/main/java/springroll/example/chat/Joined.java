package springroll.example.chat;

import java.util.Collection;

public class Joined implements ChatResponse {

    Collection<String> chatterNames;

    public Joined(Collection<String> chatterNames) {
        this.chatterNames = chatterNames;
    }

}
