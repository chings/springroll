package springroll.example.client.shell;

import akka.actor.ActorRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import springroll.example.chat.Chatter;
import springroll.example.chat.Leave;
import springroll.example.chat.Say;
import springroll.example.chat.ToJoin;
import springroll.framework.core.SpringActorSystem;
import springroll.framework.core.annotation.ActorReference;

@ShellComponent
public class ChatCommands {
    private static Logger log = LoggerFactory.getLogger(ChatCommands.class);

    public static final String DEFAULT_NAME = "Anonymous";

    @Autowired
    SpringActorSystem springActorSystem;

    @ActorReference("Chat")
    ActorRef chat;

    ActorRef chatter;

    @ShellMethod("Join the chat")
    public void join(@ShellOption(defaultValue = DEFAULT_NAME) String name) {
        chatter = springActorSystem.spawn(Chatter.class);
        SpringActorSystem.tell(chatter, new ToJoin(chat, name));
    }

    @ShellMethod("Say something")
    public void say(String content) {
        SpringActorSystem.tell(chatter, new Say(content));
    }

    @ShellMethod("Left the chat")
    public void leave() {
        SpringActorSystem.tell(chatter, new Leave());
        System.exit(0);
    }

}
