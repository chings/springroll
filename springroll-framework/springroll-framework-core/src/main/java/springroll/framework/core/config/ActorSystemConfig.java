package springroll.framework.core.config;

import akka.actor.ActorSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springroll.framework.core.*;

@Configuration
public class ActorSystemConfig {

    @Bean
    public ActorSystem actorSystem() {
        return ActorSystem.create();
    }

    @Autowired(required = false)
    Coordinator coordinator;

    @Bean
    public ActorRegistry actorRegistry() {
        return coordinator != null ?
                new CoordinatedActorRegistry(actorSystem(), coordinator) :
                new LocalActorRegistry(actorSystem());
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
