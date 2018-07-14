package springroll.framework.connector;

import java.util.Map;

public interface Frame {

    enum Method {
        CONNECT, DISCONNECT, PING, PONG, TELL, ASK, REPLY;
    }

    Method getMethod();

    String getUri();

    Map<String, String> getHeaders();

    String getHeader(String name);

    String getMessageId();

    String getContentClass();

    String getContentType();

    int getContentLength();

    Object getContent();

}
