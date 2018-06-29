package springroll.example.chat;

public class SomeoneJoined implements ChatResponse {

    String name;

    public SomeoneJoined(String name) {
        this.name = name;
    }

}
