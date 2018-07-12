package springroll.framework.connector.protocol;

public class Disconnected {

    String principal;
    String reason;

    public Disconnected() { }

    public Disconnected(String principal) {
        this.principal = principal;
    }

    public Disconnected(String principal, String reason) {
        this.principal = principal;
        this.reason = reason;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
