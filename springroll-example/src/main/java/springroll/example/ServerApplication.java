package springroll.example;

import akka.actor.ActorRef;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import springroll.example.chat.ChatActor;
import springroll.framework.annotation.ActorBean;

@SpringBootApplication
public class ServerApplication {

    @ActorBean(ChatActor.class)
    ActorRef chat;

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ServerApplication.class, args);
    }

}
