package springroll.framework.core;

import akka.actor.ActorRef;
import akka.serialization.Serialization;
import akka.serialization.SerializerWithStringManifest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

public class FastJsonSerializer extends SerializerWithStringManifest {

    public static final int IDENTIFIER = 1199;
    public static final String ENCODING = "UTF-8";

    @Override
    public int identifier() {
        return IDENTIFIER;
    }

    @Override
    public String manifest(Object o) {
        return o.getClass().getCanonicalName();
    }

    SerializeConfig serializeConfig = new SerializeConfig() {{
        put(ActorRef.class, new ObjectSerializer() {

            @Override
            public void write(JSONSerializer jsonSerializer, Object theActorRef, Object o1, Type type, int i) throws IOException {
                jsonSerializer.write(Serialization.serializedActorPath((ActorRef)theActorRef));
            }

        });
    }};

    ParserConfig parserConfig = new ParserConfig() {{
        putDeserializer(ActorRef.class, new ObjectDeserializer() {

            @Override
            public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object theActorRef) {
                //TODO: return extendedSystem.provider().resolveActorRef(dentifier);
                // https://doc.akka.io/docs/akka/2.5.4/java/serialization.html#customization
                return null;
            }

            @Override
            public int getFastMatchToken() {
                return 0;
            }

        });
    }};

    @Override
    public byte[] toBinary(Object o) {
        try {
            return JSON.toJSONString(o, serializeConfig).getBytes(ENCODING);
        } catch (UnsupportedEncodingException x) {
            return null;
        }
    }

    @Override
    public Object fromBinary(byte[] bytes, String manifest) throws NotSerializableException {
        try {
            return JSON.parseObject(new String(bytes, ENCODING), Class.forName(manifest));
        } catch (ClassNotFoundException x) {
            throw new NotSerializableException(x.getMessage());
        } catch (UnsupportedEncodingException x) {
            return null;
        }
    }

}
