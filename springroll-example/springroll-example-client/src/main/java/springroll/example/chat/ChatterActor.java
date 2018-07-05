package springroll.example.chat;

import akka.actor.ActorRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import springroll.framework.annotation.State;
import springroll.framework.core.GenericActor;

import java.util.ArrayList;
import java.util.List;

import static springroll.framework.annotation.State.BEGINNING;


public class ChatterActor extends GenericActor {
    private static Logger log = LoggerFactory.getLogger(ChatterActor.class);

    public static final String CHATTING = "chatting";

    ActorRef chat;
    String name;
    List<String> coChatterNames = new ArrayList<>();

    @State(BEGINNING)
    public void on(ToJoin toJoin) {
        chat = toJoin.chat;
        name = toJoin.name;
        Join join = new Join();
        join.senderName = name;
        join.from = this.getSelf();
        tell(chat, join);
    }

    @State({ BEGINNING, CHATTING })
    public void on(ChatterJoined chatterJoined) {
        if(CollectionUtils.isEmpty(chatterJoined.currentChatterNames)) {
            coChatterNames.add(chatterJoined.newChatterName);
        } else {
            coChatterNames.clear();
            coChatterNames.addAll(chatterJoined.currentChatterNames);
            become(CHATTING);
        }
        log.debug("{} Joined：{}", chatterJoined.newChatterName, coChatterNames);
    }

    @State(CHATTING)
    public void on(Leave leave) {
        leave.senderName = name;
        tell(chat, leave);
        terminate();
    }

    @State(CHATTING)
    public void on(ChatterLeft chatterLeft) {
        coChatterNames.remove(chatterLeft.chatterName);
        log.debug("{} Left：{}", chatterLeft.chatterName, coChatterNames);
    }

    @State(CHATTING)
    public void on(Say say) {
        say.senderName = name;
        tell(chat, say);
    }

    @State(CHATTING)
    public void on(ChatterSaid chatterSaid) {
        log.info("{} Said：{}", chatterSaid.chatterName, chatterSaid.content);
    }

}
