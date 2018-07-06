package springroll.framework.core;

import akka.actor.ActorRef;
import akka.actor.ExtendedActorSystem;
import akka.actor.RepointableActorRef;
import akka.serialization.Serialization;
import akka.serialization.SerializerWithStringManifest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.NotSerializableException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

public class FastJsonSerializer extends SerializerWithStringManifest {
    private static final Logger log = LoggerFactory.getLogger(FastJsonSerializer.class);

    public static final int IDENTIFIER = 1199;
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    ExtendedActorSystem actorSystem;

    public FastJsonSerializer(ExtendedActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    @Override
    public int identifier() {
        return IDENTIFIER;
    }

    @Override
    public String manifest(Object o) {
        return o.getClass().getCanonicalName();
    }

    ObjectSerializer actorRefSerializer = (jsonSerializer, actorRef, o1, type, i) -> {
        jsonSerializer.write(Serialization.serializedActorPath((ActorRef)actorRef));
    };

    SerializeConfig serializeConfig = new SerializeConfig() {{
        put(ActorRef.class, actorRefSerializer);
        put(RepointableActorRef.class, actorRefSerializer);
    }};

    @Override
    public byte[] toBinary(Object o) {
        return JSON.toJSONString(o, serializeConfig).getBytes(DEFAULT_CHARSET);
    }

    ObjectDeserializer actorRefDeserializer = new ObjectDeserializer() {
        @Override
        public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object name) {
            JSONLexer lexer = defaultJSONParser.getLexer();
            String value = lexer.stringVal();
            lexer.nextToken();
            return (T)Actors.resolve(actorSystem, value);
        }
        @Override
        public int getFastMatchToken() {
            return JSONToken.LITERAL_STRING;
        }
    };

    ParserConfig parseConfig = new ParserConfig() {{
        putDeserializer(ActorRef.class, actorRefDeserializer);
    }};

    @Override
    public Object fromBinary(byte[] bytes, String manifest) throws NotSerializableException {
        try {
            return JSON.parseObject(new String(bytes, DEFAULT_CHARSET), Class.forName(manifest), parseConfig);
        } catch (ClassNotFoundException x) {
            throw new NotSerializableException(x.getMessage());
        }
    }

}
