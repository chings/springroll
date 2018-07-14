package springroll.framework.connector;

import java.util.HashMap;

public class SemiMapMessage extends HashMap<String, Object> implements SemiMessage {

    public static String TO = "uri";
    public static String TYPE = "type";
    public static String PAYLOAD = "payload";

    public String getTo() {
        return (String)get(TO);
    }

    public String getType() {
        return (String)get(TYPE);
    }

    public Object getPayload() {
        return get(PAYLOAD);
    }

}
