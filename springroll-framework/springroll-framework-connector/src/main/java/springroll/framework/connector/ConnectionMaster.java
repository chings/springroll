package springroll.framework.connector;

import akka.actor.ActorRef;
import springroll.framework.connector.protocol.Connected;
import springroll.framework.connector.protocol.Disconnected;
import springroll.framework.core.GenericActor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionMaster extends GenericActor {

    Map<String, ActorRef> connections = new ConcurrentHashMap<>();

    public void on(Connected connected) {
        ActorRef connection = spawn(connected.getPrincipal(), Connection.class);
        connections.put(connected.getPrincipal(), connection);
        tell(connection, connected);
    }

    public void on(Disconnected disconnected) {
        connections.remove(disconnected.getPrincipal());
    }

}
