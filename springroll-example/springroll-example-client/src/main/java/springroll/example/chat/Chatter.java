package springroll.example.chat;

import akka.actor.ActorRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import springroll.framework.core.GenericActor;
import springroll.framework.core.annotation.At;

import java.util.ArrayList;
import java.util.List;

import static springroll.framework.core.annotation.At.BEGINNING;

public class Chatter extends GenericActor {
    private static Logger log = LoggerFactory.getLogger(Chatter.class);

    public static final String CHATTING = "chatting";

    ActorRef chat;
    String name;
    List<String> coChatterNames = new ArrayList<>();

    @At(BEGINNING)
    public void on(ToJoin toJoin) {
        chat = toJoin.chat;
        name = toJoin.name;
        Join join = new Join();
        join.chatterName = name;
        tell(chat, join);
    }

    @At(BEGINNING)
    public void on(Joined joined) {
        coChatterNames.clear();
        coChatterNames.addAll(joined.allChatterNames);
        log.info("Joined：{}", coChatterNames);
        become(CHATTING);
    }

    @At(BEGINNING)
    public void on(NotJoined notJoined) {
        log.warn("NotJoined：{}", notJoined.getReason());
    }

    @At(CHATTING)
    public void on(ChatterJoined chatterJoined) {
        coChatterNames.add(chatterJoined.chatterName);
        log.info("{} Joined：{}", chatterJoined.chatterName, coChatterNames);
    }

    @At(CHATTING)
    public void on(Leave leave) {
        tell(chat, leave);
        terminate();
    }

    @At(CHATTING)
    public void on(ChatterLeft chatterLeft) {
        coChatterNames.remove(chatterLeft.chatterName);
        log.debug("{} Left：{}", chatterLeft.chatterName, coChatterNames);
    }

    @At(CHATTING)
    public void on(Say say) {
        tell(chat, say);
    }

    @At(CHATTING)
    public void on(ChatterSaid chatterSaid) {
        log.info("{} Said：{}", chatterSaid.chatterName, chatterSaid.content);
    }

}
