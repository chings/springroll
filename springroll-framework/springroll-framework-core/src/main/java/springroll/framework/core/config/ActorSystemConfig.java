package springroll.framework.core.config;

import akka.actor.ActorSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springroll.framework.core.*;

import javax.annotation.PreDestroy;

@Configuration
public class ActorSystemConfig {

    @Value("${application.name:springroll}")
    String applicationName;

    @Bean
    public ActorSystem actorSystem() {
        return ActorSystem.create(applicationName);
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
