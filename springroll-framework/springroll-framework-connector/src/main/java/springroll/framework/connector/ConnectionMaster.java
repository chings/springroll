package springroll.framework.connector;

import akka.actor.ActorRef;
import springroll.framework.connector.protocol.Connected;
import springroll.framework.core.GenericActor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionMaster extends GenericActor {

    Map<String, ActorRef> connections = new ConcurrentHashMap<>();

    public void on(Connected connected) {
        ActorRef connection = spawn(Connection.class);
        connections.put(connected.getPrincipal(), connection);
        tell(connection, connected);
    }

}
