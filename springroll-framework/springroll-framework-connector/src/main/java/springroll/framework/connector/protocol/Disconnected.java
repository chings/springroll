package springroll.framework.connector.protocol;

public class Disconnected {

    String principalName;
    String reason;

    public Disconnected() { }

    public Disconnected(String principalName) {
        this.principalName = principalName;
    }

    public Disconnected(String principalName, String reason) {
        this.principalName = principalName;
        this.reason = reason;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
