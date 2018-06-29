package springroll.example.chat;

public class Leave implements ChatRequest {

    String name;

    public Leave(String name) {
        this.name = name;
    }

}
