package springroll.framework.connector;

import com.alibaba.fastjson.JSON;
import org.springframework.web.reactive.socket.WebSocketMessage;
import springroll.framework.connector.util.Classes;

import java.nio.charset.Charset;

public class FastJsonMarshaller implements Marshaller {

    Charset charset = Charset.forName("UTF-8");

    @Override
    public String marshal(Object message) {
        return JSON.toJSONString(message);
    }

    @Override
    public SemiMessage normalize(WebSocketMessage rawMessage) {
        return JSON.parseObject(rawMessage.getPayloadAsText(charset), SemiMapMessage.class);
    }

    @Override
    public Object unmarshal(SemiMessage semiMessage) {
        String type = semiMessage.getType();
        if(type == null) return null;
        Class<?> messageClass = Classes.guess(type);
        if(messageClass == null) return null;
        return JSON.parseObject(JSON.toJSONString(semiMessage.getPayload()), messageClass);
    }

}
