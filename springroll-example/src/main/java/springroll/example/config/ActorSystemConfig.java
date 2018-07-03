package springroll.example.config;

import akka.actor.ActorSystem;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springroll.framework.core.ActorBeanPostProcessor;
import springroll.framework.core.ActorReferencePostProcessor;
import springroll.framework.core.ActorRegistry;

@Configuration
public class ActorSystemConfig {

    @Bean
    public ActorSystem actorSystem() {
        return ActorSystem.create();
    }

    @Bean
    public ActorRegistry actorRegistry() {
        return new ActorRegistry(actorSystem());
    }

    @Bean
    public BeanPostProcessor ActorBeanPostProcessor() {
        return new ActorBeanPostProcessor(actorSystem(), actorRegistry());
    }

    @Bean
    public BeanPostProcessor ActorReferencePostProcessor() {
        return new ActorReferencePostProcessor(actorRegistry());
    }

}
