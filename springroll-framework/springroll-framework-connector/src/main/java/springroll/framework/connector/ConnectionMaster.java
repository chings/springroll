package springroll.framework.connector;

import akka.actor.ActorRef;
import org.springframework.beans.factory.annotation.Autowired;
import springroll.framework.connector.protocol.Connected;
import springroll.framework.connector.protocol.Disconnected;
import springroll.framework.core.GenericActor;
import springroll.framework.core.SpringActorSystem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionMaster extends GenericActor {

    @Autowired
    SpringActorSystem springActorSystem;

    Map<String, ActorRef> connections = new ConcurrentHashMap<>();

    public void on(Connected connected) {
        String principalName = connected.getPrincipalName();
        ActorRef connection = spawn(principalName, springActorSystem.props("connection"));
        connections.put(principalName, connection);
        tell(connection, connected);
    }

    public void on(Disconnected disconnected) {
        connections.remove(disconnected.getPrincipalName());
    }

}
