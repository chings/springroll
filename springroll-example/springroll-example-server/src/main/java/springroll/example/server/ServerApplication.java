package springroll.example.server;

import akka.actor.ActorRef;
import org.jline.utils.AttributedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.jline.PromptProvider;
import springroll.example.chat.ChatActor;
import springroll.framework.coordinator.annotation.EnableZkCoordinator;
import springroll.framework.core.annotation.ActorBean;
import springroll.framework.core.annotation.EnableActorSystem;

@SpringBootApplication
@EnableZkCoordinator
@EnableActorSystem
public class ServerApplication {
    private static Logger log = LoggerFactory.getLogger(ServerApplication.class);

    @ActorBean(value = ChatActor.class, name = "chat")
    ActorRef chat;

    @Value("${application.name}")
    String applicationName;

    @Bean
    public PromptProvider promptProvider() {
        return () -> new AttributedString(applicationName + ">");
    }

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

}
