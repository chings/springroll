package springroll.framework.core;

import akka.actor.Actor;
import akka.actor.IndirectActorProducer;
import org.springframework.context.ApplicationContext;

public class SpringActorProducer implements IndirectActorProducer {

    ApplicationContext applicationContext;
    String beanName;

    public SpringActorProducer(ApplicationContext applicationContext, String beanName) {
        this.applicationContext = applicationContext;
        this.beanName = beanName;
    }

    @Override
    public Actor produce() {
        return (Actor)applicationContext.getBean(beanName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Actor> actorClass() {
        return (Class<? extends Actor>)applicationContext.getType(beanName);
    }

}