package springroll.framework.coordinator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import springroll.framework.core.Coordinator;

import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Consumer;

import static springroll.framework.core.CoordinatedActorRegistry.split;

public class RedisCoordinator implements Coordinator, MessageListener {

    public static final String PROVIDE = "PROVIDE";
    public static final String UNPROVIDE = "UNPROVIDE";
    public static final String UNPROVIDE_ALL = "UNPROVIDE_ALL";

    String topic;
    String keyPrefix = "SR:COORDINATION";

    @Autowired
    StringRedisTemplate redisTemplate;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String key() {
        return keyPrefix;
    }

    public String key(String keyInfo) {
        return keyPrefix + ":" + keyInfo;
    }

    public String event(String tag, String actorPath) {
        return tag + ":" + actorPath;
    }

    @Override
    public void provide(String actorPath) {
        String[] tuple = split(actorPath);
        String host = tuple[0], shortPath = tuple[1];
        redisTemplate.opsForSet().add(key(), host);
        redisTemplate.opsForSet().add(key(host), shortPath);
        redisTemplate.convertAndSend(topic, event(PROVIDE, actorPath));
    }

    @Override
    public void unprovide(String actorPath) {
        String[] tuple = split(actorPath);
        String host = tuple[0], shortPath = tuple[1];
        redisTemplate.opsForSet().remove(key(host), shortPath);
        if(redisTemplate.opsForSet().size(key(host)) == 0) {
            redisTemplate.opsForSet().remove(key(), host);
        }
        redisTemplate.convertAndSend(topic, event(UNPROVIDE, actorPath));
    }

    @Override
    public void unprovideAll(String host) {
        redisTemplate.delete(key(host));
        redisTemplate.opsForSet().remove(key(), host);
        redisTemplate.convertAndSend(topic, event(UNPROVIDE_ALL, host));
    }

    @Override
    public void synchronize(Consumer<List<String>> handler) {
        List<String> all = new ArrayList<>();
        Set<String> hosts = redisTemplate.opsForSet().members(key());
        for(String host : hosts) {
            Set<String> shortPaths = redisTemplate.opsForSet().members(key(host));
            for(String shortPath : shortPaths) {
                all.add(host + shortPath);
            }
        }
        handler.accept(all);
    }

    Map<String, Consumer<String>> handlers = new HashMap<>();

    @Override
    public void listenProvide(Consumer<String> handler) {
        handlers.put(UNPROVIDE, handler);
    }

    @Override
    public void listenUnProvide(Consumer<String> handler) {
        handlers.put(UNPROVIDE, handler);
    }

    @Override
    public void listenUnProvideAll(Consumer<String> handler) {
        handlers.put(UNPROVIDE_ALL, handler);
    }

    @Override
    public void onMessage(Message message, byte[] bytes) {
        String event = new String(bytes, Charset.forName("UTF-8"));
        String[] tuple = event.split(":", 2);
        if(tuple.length != 2) return;
        Consumer<String> handler = handlers.get(tuple[0]);
        if(handler != null) handler.accept(tuple[1]);
    }

}