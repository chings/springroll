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
import springroll.example.chat.Chat;
import springroll.framework.connector.annotation.EnableWebSocketConnector;
import springroll.framework.core.annotation.ActorBean;
import springroll.framework.core.annotation.EnableActorSystem;

@SpringBootApplication
@EnableActorSystem
@EnableWebSocketConnector
public class ServerApplication {
    private static Logger log = LoggerFactory.getLogger(ServerApplication.class);

    @ActorBean(actorClass = Chat.class)
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
