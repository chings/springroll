package springroll.example.config;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class ActorSystemConfig {

    @Bean
    public Config akkaConfiguration() {
        return ConfigFactory.load();
    }

    @Value("${akka.system.name}")
    String name;

    @Bean
    public ActorSystem actorSystem(Config config) {
        ActorSystem system = ActorSystem.create(name, config);
        return system;
    }

    @Bean
    public BeanPostProcessor BeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                return null;
            }
        };
    }

}
