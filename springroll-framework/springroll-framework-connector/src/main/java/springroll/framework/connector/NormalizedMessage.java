package springroll.framework.connector;

import java.util.HashMap;

public class NormalizedMessage extends HashMap<String, Object> {

    public static String TO = "to";
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
