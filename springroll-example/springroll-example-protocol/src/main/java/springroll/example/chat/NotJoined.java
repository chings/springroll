package springroll.example.chat;

import springroll.framework.protocol.UnjoinMessage;

import java.io.Serializable;

public class NotJoined implements UnjoinMessage, Serializable {

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
