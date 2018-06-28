package springroll.example;

import akka.actor.ActorRef;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springroll.example.model.Mora;
import springroll.example.model.MoraMaster;
import springroll.framework.annotation.ActorReference;

@SpringBootApplication
public class ServerApplication {

    @ActorReference
    MoraMaster moraMaster;

    @ActorReference(Mora.class)
    ActorRef ref;

    public static void main(String[] args) {

    }

}
