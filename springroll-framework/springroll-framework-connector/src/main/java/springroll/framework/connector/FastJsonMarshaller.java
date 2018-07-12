package springroll.framework.connector;

import com.alibaba.fastjson.JSON;
import org.springframework.web.reactive.socket.WebSocketMessage;

import java.nio.charset.Charset;

public class FastJsonMarshaller implements Marshaller {

    Charset charset = Charset.forName("UTF-8");

    @Override
    public String marshal(Object message) {
        return JSON.toJSONString(message);
    }

    @Override
    public SemiMessage unmarshal(WebSocketMessage rawMessage) {
        return JSON.parseObject(rawMessage.getPayloadAsText(charset), SemiMapMessage.class);
    }

    @Override
    public Object unmarshal(Class<?> messageClass, Object payload) {
        return JSON.parseObject(JSON.toJSONString(payload), messageClass);
    }

}
