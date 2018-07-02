package springroll.example.shell;

import akka.actor.ActorRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import springroll.example.chat.*;
import springroll.framework.GenericActor;

import java.util.ArrayList;
import java.util.List;

public class ChatterActor extends GenericActor {
    private static Logger log = LoggerFactory.getLogger(ChatterActor.class);

    ActorRef chat;
    String name;
    List<String> coChatterNames;

    public ChatterActor(ActorRef chat, String name) {
        this.chat = chat;
        this.name = name;
        this.coChatterNames = new ArrayList<>();
    }

    public void on(Join join) {
        join.setSenderName(name);
        tell(chat, join);
    }

    public void on(ChatterJoined chatterJoined) {
        if(!CollectionUtils.isEmpty(chatterJoined.getCurrentChatterNames())) {
            coChatterNames.clear();
            coChatterNames.addAll(chatterJoined.getCurrentChatterNames());
        }
        coChatterNames.add(chatterJoined.getNewChatterName());
        log.debug("{} Joined：{}", chatterJoined.getNewChatterName(), coChatterNames);
    }

    public void on(Leave leave) {
        leave.setSenderName(name);
        tell(chat, leave);
    }

    public void on(ChatterLeft chatterLeft) {
        coChatterNames.remove(chatterLeft.getChatterName());
        log.debug("{} Left：{}", chatterLeft.getChatterName(), coChatterNames);
    }

    public void on(Say say) {
        say.setSenderName(name);
        tell(chat, say);
    }

    public void on(ChatterSaid chatterSaid) {
        log.info("{} Said：{}", chatterSaid.getChatterName(), chatterSaid.getContent());
    }

}
