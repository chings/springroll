package springroll.example.chat;

public class Said implements ChatResponse {

    String name;
    String message;

    public Said(String name, String message) {
        this.name = name;
        this.message = message;
    }

}
