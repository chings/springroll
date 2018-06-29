package springroll.example.chat;

public class Say implements ChatRequest {

    String name;
    String message;

    public Say(String name, String message) {
        this.name = name;
        this.message = message;
    }

}
