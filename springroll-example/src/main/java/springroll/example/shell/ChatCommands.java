package springroll.example.shell;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import springroll.example.chat.*;
import springroll.framework.annotation.ActorReference;

import static springroll.framework.core.Actors.spawn;
import static springroll.framework.core.Actors.tell;

@ShellComponent
public class ChatCommands {
    private static Logger log = LoggerFactory.getLogger(ChatCommands.class);

    ActorRef chatter;

    @Autowired
    ActorSystem actorSystem;

    @ActorReference(ChatActor.class)
    ActorRef chat;

    @ShellMethod("Join the chat")
    public void join(String name) {
        chatter = spawn(actorSystem, ChatterActor.class, chat, name);
        tell(chatter, new Join());
    }

    @ShellMethod("Say something")
    public void say(String content) {
        tell(chatter, new Say(content));
    }

    @ShellMethod("Left the chat")
    public void leave() {
        tell(chatter, new Leave());
    }

}
