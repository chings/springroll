package springroll.example.chat;

import akka.actor.ActorRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import springroll.framework.core.Actorlet;
import springroll.framework.core.annotation.Initial;
import springroll.framework.core.annotation.State;

import java.util.ArrayList;
import java.util.List;

public class Chatter extends Actorlet {
    private static Logger log = LoggerFactory.getLogger(Chatter.class);

    ActorRef chat;
    String name;
    List<String> coChatterNames = new ArrayList<>();

    @Initial
    @State
    public class JoiningState {

        public void on(ToJoin toJoin) {
            chat = toJoin.chat;
            name = toJoin.name;
            Join join = new Join();
            join.chatterName = name;
            tell(chat, join);
        }

        public Object on(Joined joined) {
            coChatterNames.clear();
            coChatterNames.addAll(joined.allChatterNames);
            log.info("Joined：{}", coChatterNames);
            return "chatting";
        }

        public void on(NotJoined notJoined) {
            log.warn("NotJoined：{}", notJoined.getReason());
        }

    }

    @State("chatting")
    public class ChattingState {

        public void on(Say say) {
            tell(chat, say);
        }

        public void on(Leave leave) {
            tell(chat, leave);
            terminate();
        }

        public void on(ChatterJoined chatterJoined) {
            coChatterNames.add(chatterJoined.chatterName);
            log.info("{} Joined：{}", chatterJoined.chatterName, coChatterNames);
        }

        public void on(ChatterLeft chatterLeft) {
            coChatterNames.remove(chatterLeft.chatterName);
            log.debug("{} Left：{}", chatterLeft.chatterName, coChatterNames);
        }

        public void on(ChatterSaid chatterSaid) {
            log.info("{} Said：{}", chatterSaid.chatterName, chatterSaid.content);
        }

    }

}
