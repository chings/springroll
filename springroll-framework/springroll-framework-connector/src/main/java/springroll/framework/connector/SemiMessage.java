package springroll.framework.connector;

public interface SemiMessage {

    String getTo();
    String getType();
    Object getPayload();

}
