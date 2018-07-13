package springroll.example.chat;

public class NotJoined {

    String reason;

    public NotJoined() {
    }

    public NotJoined(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
