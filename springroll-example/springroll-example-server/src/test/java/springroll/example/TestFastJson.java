package springroll.example;

import akka.actor.ActorRef;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import springroll.example.chat.Join;

import java.lang.reflect.Type;

@RunWith(JUnit4.class)
public class TestFastJson {
    private Logger log = LoggerFactory.getLogger(TestFastJson.class);

    ParserConfig parseConfig = new ParserConfig() {{
        putDeserializer(ActorRef.class, new ObjectDeserializer() {
            @Override
            public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object o) {
                log.info("{}, {}, {}", type, o, defaultJSONParser.getLexer().stringVal());
                defaultJSONParser.getLexer().nextToken();
                return (T)null;
            }
            @Override
            public int getFastMatchToken() {
                return JSONToken.LITERAL_STRING;
            }
        });
    }};

    @Test
    public void test1() {
        String s = "{\"from\":\"akka.tcp://default@127.0.0.1:62540\",\"senderName\":\"ching\"}";
        Join join = JSON.parseObject(s, Join.class, parseConfig);
        assert join != null;
    }

}
