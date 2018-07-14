package springroll.framework.connector;

import java.util.HashMap;
import java.util.Map;

/**
 * An example -
 * <p>
 * TELL /chats/80
 * Content-Length: 18
 * Content-Type: application/json
 * Content-Class: Say
 * <p>
 * {content:"Hello!"}
 */
public class HttpLikeFrame implements Frame {

    public static final String MESSAGE_ID = "Message-Id";
    public static final String CONTENT_CLASS = "Content-Class";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_LENGTH = "Content-Length";

    Method method;
    String uri;
    Map<String, String> headers = new HashMap<>();
    Object content;

    @Override
    public Method getMethod() {
        return null;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    @Override
    public String getUri() {
        return null;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public String getHeader(String name) {
        return headers.get(name);
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    @Override
    public String getMessageId() {
        return headers.get(MESSAGE_ID);
    }

    public void setMessageId(String messageId) {
        headers.put(MESSAGE_ID, messageId);
    }

    @Override
    public String getContentClass() {
        return headers.get(CONTENT_CLASS);
    }

    public void setContentClass(String contentClass) {
        headers.put(CONTENT_CLASS, contentClass);
    }

    @Override
    public String getContentType() {
        return headers.get(CONTENT_TYPE);
    }

    public void setContentType(String contentType) {
        headers.put(CONTENT_TYPE, contentType);
    }

    @Override
    public int getContentLength() {
        try {
            return Integer.parseInt(getHeader("Content-Length"));
        } catch(Exception x) {
            return -1;
        }
    }

    public void setContentLength(int contentLength) {
        headers.put(CONTENT_LENGTH, String.valueOf(contentLength));
    }

    @Override
    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

}
