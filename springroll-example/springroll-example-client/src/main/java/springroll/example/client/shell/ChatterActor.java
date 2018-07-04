package springroll.example.client.shell;

import akka.actor.ActorSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import springroll.example.chat.*;
import springroll.framework.annotation.State;
import springroll.framework.core.GenericActor;

import java.util.ArrayList;
import java.util.List;

import static springroll.framework.annotation.State.BEGINNING;


public class ChatterActor extends GenericActor {
    private static Logger log = LoggerFactory.getLogger(ChatterActor.class);

    public static final String CHATTING = "chatting";

    ActorSelection chat;
    String name;
    List<String> coChatterNames = new ArrayList<>();

    @State(BEGINNING)
    public void on(ToJoin toJoin) {
        chat = toJoin.chat;
        name = toJoin.name;
        Join join = new Join();
        join.setSenderName(name);
        join.setFrom(this.getSelf());
        tell(chat, join);
    }

    @State({ BEGINNING, CHATTING })
    public void on(ChatterJoined chatterJoined) {
        if(CollectionUtils.isEmpty(chatterJoined.getCurrentChatterNames())) {
            coChatterNames.add(chatterJoined.getNewChatterName());
        } else {
            coChatterNames.clear();
            coChatterNames.addAll(chatterJoined.getCurrentChatterNames());
            become(CHATTING);
        }
        log.debug("{} Joined：{}", chatterJoined.getNewChatterName(), coChatterNames);
    }

    @State(CHATTING)
    public void on(Leave leave) {
        leave.setSenderName(name);
        tell(chat, leave);
        become(BEGINNING);
    }

    @State(CHATTING)
    public void on(ChatterLeft chatterLeft) {
        coChatterNames.remove(chatterLeft.getChatterName());
        log.debug("{} Left：{}", chatterLeft.getChatterName(), coChatterNames);
    }

    @State(CHATTING)
    public void on(Say say) {
        say.setSenderName(name);
        tell(chat, say);
    }

    @State(CHATTING)
    public void on(ChatterSaid chatterSaid) {
        log.info("{} Said：{}", chatterSaid.getChatterName(), chatterSaid.getContent());
    }

}
