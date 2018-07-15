package springroll.framework.core;

import akka.actor.*;
import akka.serialization.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public class SpringActorSystem implements Extension {
    private static Logger log = LoggerFactory.getLogger(SpringActorSystem.class);

    ApplicationContext applicationContext;
    ExtendedActorSystem actorSystem;

    public SpringActorSystem(ApplicationContext applicationContext, ExtendedActorSystem actorSystem) {
        this.applicationContext = applicationContext;
        this.actorSystem = actorSystem;
    }

    public Props props(String beanName) {
        return Props.create(SpringActorProducer.class, applicationContext, beanName);
    }

    public ActorRef spawn(String name, Props props) {
        return actorSystem.actorOf(props, name);
    }

    public ActorRef spawn(Props props) {
        return actorSystem.actorOf(props);
    }

    public ActorRef spawn(String name, String beanName) {
        return actorSystem.actorOf(props(beanName), name);
    }

    public ActorRef spawn(String beanName) {
        return actorSystem.actorOf(props(beanName), beanName);
    }

    public ActorRef spawn(String name, Class<? extends Actor> actorClass, Object... args) {
        return actorSystem.actorOf(Props.create(actorClass, args), name);
    }

    public ActorRef spawn(Class<? extends Actor> actorClass, Object... args) {
        return actorSystem.actorOf(Props.create(actorClass, args), actorClass.getSimpleName());
    }

    public ActorRef resolve(String actorPath) {
        return actorSystem.provider().resolveActorRef(actorPath);
    }

    public ActorSelection select(String actorPath) {
        return actorSystem.actorSelection(actorPath);
    }

    @SuppressWarnings("deprecated")
    public String getHost() {
        String rootPath = Serialization.serializedActorPath(actorSystem.actorFor("/"));
        return rootPath.substring(0, rootPath.length() - 1);
    }

}
