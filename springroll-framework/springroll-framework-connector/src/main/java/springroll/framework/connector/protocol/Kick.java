package springroll.framework.connector.protocol;

import java.io.Serializable;

public class Kick implements Serializable {

    String reason;

    public Kick() {
    }

    public Kick(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
