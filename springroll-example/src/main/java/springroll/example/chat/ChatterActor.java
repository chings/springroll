package springroll.example.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import springroll.framework.AdaptiveActor;
import springroll.framework.annotation.ActorComponent;

import java.util.ArrayList;
import java.util.List;

@ActorComponent("chatter")
public class ChatterActor extends AdaptiveActor {
    private static Logger log = LoggerFactory.getLogger(ChatterActor.class);

    String myName;
    List<String> coChatterNames;

    public ChatterActor(String name) {
        myName = name;
        coChatterNames = new ArrayList<>();
    }

    public void on(ChatterJoined chatterJoined) {
        if(CollectionUtils.hasUniqueObject(chatterJoined.currentChatterNames)) {
            coChatterNames.clear();
            coChatterNames.addAll(chatterJoined.currentChatterNames);
        }
        coChatterNames.add(chatterJoined.newChatterName);
        log.debug("{} Joined：{}", chatterJoined.newChatterName, coChatterNames);
    }

    public void on(ChatterLeft chatterLeft) {
        coChatterNames.remove(chatterLeft.chatterName);
        log.debug("{} Left：{}", chatterLeft.chatterName, coChatterNames);
    }

    public void on(ChatterSaid chatterSaid) {
        log.info("{} Said：{}", chatterSaid.chatterName, chatterSaid.content);
    }

}
