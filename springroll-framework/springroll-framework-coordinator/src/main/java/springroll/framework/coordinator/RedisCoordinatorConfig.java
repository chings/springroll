package springroll.framework.coordinator;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import springroll.framework.core.Coordinator;

import java.util.*;

@Configuration
public class RedisCoordinatorConfig {

    @Bean
    @ConfigurationProperties("springroll.coordination.redis")
    public Coordinator redisCoordinator() {
        return new RedisCoordinator();
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory,
            RedisCoordinator redisCoordinator) {
        RedisMessageListenerContainer bean = new RedisMessageListenerContainer();
        bean.setConnectionFactory(redisConnectionFactory);
        Map<MessageListener, Collection<? extends Topic>> messageListeners = new HashMap<>();
        List<Topic> topics = new ArrayList<>();
        topics.add(new ChannelTopic(redisCoordinator.getTopic()));
        messageListeners.put(redisCoordinator, topics);
        bean.setMessageListeners(messageListeners);
        return bean;
    }

}
