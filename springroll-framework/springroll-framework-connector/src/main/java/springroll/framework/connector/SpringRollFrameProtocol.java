package springroll.framework.connector;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import springroll.framework.connector.util.Classes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * (An incoming sample)
 * ASK /chats/80
 * Serial-No: 1
 * Content-Length: 18
 * Content-Type: application/json
 * Content-Class: chat.Hello
 *
 * {content:"Hello!"}
 *
 *
 * (An outgoing sample)
 * /chats/80 REPLY
 * Re-Serial-No: 1
 * Content-Length: 18
 * Content-Type: application/json
 * Content-Class: chat.Welcome
 *
 * {content:"Welcome!"}
 */
public class SpringRollFrameProtocol implements FrameProtocol {
    private static Logger log = LoggerFactory.getLogger(SpringRollFrameProtocol.class);

    public static final String CRLF = "\r\n";

    @Override
    public Frame marshal(Object message) {
        Frame frame = new Frame();
        String content = JSON.toJSONString(message);
        frame.setContentClass(message.getClass().getCanonicalName());
        frame.setContentType("application/json");
        frame.setContentLength(content.getBytes().length);
        frame.setContent(content);
        return frame;
    }

    @Override
    public Object unmarshal(Frame frame) {
        Class<?> contentClass = Classes.guess(frame.getContentClass());
        return JSON.parseObject(frame.getContent(), contentClass);
    }

    @Override
    public String serialize(Frame frame) {
        StringWriter out = new StringWriter();
        if(StringUtils.hasText(frame.getUri())) {
            out.write(noUserPrefix(frame.getUri()));
            out.write(' ');
        }
        out.write(frame.method.toString());
        out.write(CRLF);
        for(Map.Entry<String, String> entry : frame.headers.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            if(StringUtils.hasText(value)) {
                out.write(name.trim());
                out.write(": ");
                out.write(value.trim());
                out.write(CRLF);
            }
        }
        out.write(CRLF);
        out.write(frame.getContent());
        return out.toString();
    }

    public static String noUserPrefix(String uri) {
        return uri.startsWith("/user/") ? uri.substring(5) : uri;
    }

    @Override
    @SuppressWarnings("deprecated")
    public Frame unseralize(String serialized) {
        BufferedReader reader = new BufferedReader(new StringReader(serialized));
        try {
            String line = reader.readLine();
            String[] parts = line.split(" ", 2);
            Frame.Method method = Frame.Method.valueOf(parts[0].trim());
            Frame frame = new Frame(method);
            frame.setUri(parts[1].trim());
            while(StringUtils.hasText(line = reader.readLine())) {
                parts = line.split(":", 2);
                frame.setHeader(parts[0].trim(), parts[1].trim());
            }
            String content = IOUtils.readAll(reader);
            frame.setContent(content);
            return frame;
        } catch(IOException x) {
            log.warn("Ugh! {}", x.getMessage(), x);
            return null;
        }
    }

}