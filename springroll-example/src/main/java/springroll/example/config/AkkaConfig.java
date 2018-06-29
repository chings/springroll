package springroll.example.config;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springroll.framework.annotation.ActorComponent;

import javax.annotation.PostConstruct;
import java.util.Map;

@Configuration
public class AkkaConfig {

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

    @Autowired
    ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(ActorComponent.class);
    }

}
