package springroll.framework.connector;

import java.util.HashMap;
import java.util.Map;


public class Frame {

    enum Method {
        CONNECT, DISCONNECT, PING, PONG, TELL, ASK, REPLY, ERROR
    }

    public static final String SERIAL_NO = "Serial-No";
    public static final String RE_SERIAL_NO = "Re-Serial-No";
    public static final String CONTENT_CLASS = "Content-Class";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_LENGTH = "Content-Length";

    Method method;
    String uri;
    Map<String, String> headers = new HashMap<>();
    String content = "";

    public Frame() { }

    public Frame(Method method) {
        this.method = method;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public String getSerialNo() {
        return headers.get(SERIAL_NO);
    }

    public void setSerialNo(String serialNo) {
        headers.put(SERIAL_NO, serialNo);
    }

    public String getReSerialNo() {
        return headers.get(RE_SERIAL_NO);
    }

    public void setReSerialNo(String serialNo) {
        headers.put(SERIAL_NO, RE_SERIAL_NO);
    }

    public String getContentType() {
        return headers.get(CONTENT_TYPE);
    }

    public void setContentType(String contentType) {
        headers.put(CONTENT_TYPE, contentType);
    }

    public int getContentLength() {
        try {
            return Integer.valueOf(headers.get(CONTENT_LENGTH));
        } catch(Exception x) {
            return -1;
        }
    }

    public void setContentLength(int contentLength) {
        headers.put(CONTENT_LENGTH, String.valueOf(contentLength));
    }

    public String getContentClass() {
        return headers.get(CONTENT_CLASS);
    }

    public void setContentClass(String contentClass) {
        headers.put(CONTENT_CLASS, contentClass);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Frame{" +
                "method=" + method +
                ", uri='" + uri + '\'' +
                ", headers=" + headers +
                ", content='" + content + '\'' +
                '}';
    }

}
