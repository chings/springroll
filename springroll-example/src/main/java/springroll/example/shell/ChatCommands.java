package springroll.example.shell;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import springroll.example.chat.*;
import springroll.framework.annotation.ActorReference;

import javax.annotation.PostConstruct;

import static springroll.framework.core.Actors.spawn;
import static springroll.framework.core.Actors.tell;

@ShellComponent
public class ChatCommands {
    private static Logger log = LoggerFactory.getLogger(ChatCommands.class);

    public static final String DEFAULT_NAME = "Anonymous";

    @Autowired
    ActorSystem actorSystem;

    @ActorReference(ChatActor.class)
    ActorRef chat;

    ActorRef chatter;

    @PostConstruct
    public void init() {
        chatter = spawn(actorSystem, ChatterActor.class, chat);
    }

    @ShellMethod("Join the chat")
    public void join(@ShellOption(defaultValue = DEFAULT_NAME) String name) {
        tell(chatter, new ChangeName(name));
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
