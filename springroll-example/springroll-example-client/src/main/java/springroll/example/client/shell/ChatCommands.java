package springroll.example.client.shell;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import springroll.example.chat.ChatterActor;
import springroll.example.chat.Leave;
import springroll.example.chat.Say;
import springroll.example.chat.ToJoin;

import javax.annotation.PostConstruct;

import static springroll.framework.core.Actors.spawn;
import static springroll.framework.core.Actors.tell;

@ShellComponent
public class ChatCommands {
    private static Logger log = LoggerFactory.getLogger(ChatCommands.class);

    public static final String DEFAULT_NAME = "Anonymous";

    @Autowired
    ActorSystem actorSystem;

    ActorSelection chat;
    ActorRef chatter;

    @PostConstruct
    public void init() {
        chat = actorSystem.actorSelection("akka.tcp://default@127.0.0.1:2552/user/chat");
        chatter = spawn(actorSystem, ChatterActor.class);
    }

    @ShellMethod("ToJoin the chat")
    public void join(@ShellOption(defaultValue = DEFAULT_NAME) String name) {
        tell(chatter, new ToJoin(chat, name));
    }

    @ShellMethod("Say something")
    public void say(String content) {
        tell(chatter, new Say(content));
    }

    @ShellMethod("Left the chat")
    public void leave() {
        tell(chatter, new Leave());
        System.exit(0);
    }

}
