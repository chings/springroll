package springroll.framework.connector.protocol;

public class Disconnected {

    String principal;

    public Disconnected() { }

    public Disconnected(String principal) {
        this.principal = principal;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

}
