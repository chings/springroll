package springroll.example.chat;

public class SomeoneLeft implements ChatResponse {

    String name;

    public SomeoneLeft(String name) {
        this.name = name;
    }

}
