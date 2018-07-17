package springroll.framework.core.config;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.util.StringUtils;
import springroll.framework.core.*;
import springroll.framework.core.util.Settings;

import javax.annotation.PreDestroy;

@Configuration
public class ActorSystemConfig {

    @Value("${application.name:springroll}")
    String applicationName;

    @Autowired
    AbstractEnvironment environment;

    @Bean
    public ActorSystem actorSystem() {
        Config premadeConfig = ConfigFactory.load();
        StringBuilder configLines = new StringBuilder();
        Settings.forEach(environment, (key, value) -> {
            if(key.startsWith("akka.") && StringUtils.hasText(value.toString())) {
                configLines.append(key).append("=").append(value).append("\n");
            }
        });
        Config additionalConfig = ConfigFactory.parseString(configLines.toString());
        Config finalConfig = additionalConfig.withFallback(premadeConfig);
        return ActorSystem.create(applicationName, ConfigFactory.load(finalConfig));
    }

    @Bean
    SpringActorSystemFactory springActorSystem() {
        return new SpringActorSystemFactory();
    }

    @ConditionalOnProperty("springroll.coordinator.zk.connectionString")
    @Bean
    @ConfigurationProperties(prefix = "springroll.coordinator.zk")
    public Coordinator zkCoordinator() {
        return new ZkCoordinator();
    }

    @Bean
    public ActorRegistry actorRegistry() {
        return new CoordinatedActorRegistry();
    }

    @Bean
    public BeanPostProcessor ActorBeanPostProcessor() {
        return new ActorBeanPostProcessor();
    }

    @Bean
    public BeanPostProcessor ActorReferencePostProcessor() {
        return new ActorReferencePostProcessor();
    }

    @PreDestroy
    public void destroy() {
        actorSystem().terminate();
    }

}
