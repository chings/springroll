package springroll.framework.core;

import akka.actor.AbstractExtensionId;
import akka.actor.ActorSystem;
import akka.actor.ExtendedActorSystem;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringActorSystemFactory extends AbstractExtensionId<SpringActorSystem>
        implements ApplicationContextAware, FactoryBean<SpringActorSystem> {

    ApplicationContext applicationContext;

    @Autowired
    ActorSystem actorSystem;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public SpringActorSystem createExtension(ExtendedActorSystem system) {
        return new SpringActorSystem(applicationContext, actorSystem);
    }

    @Override
    public Class<?> getObjectType() {
        return SpringActorSystem.class;
    }

    @Override
    public SpringActorSystem getObject() {
        return get(actorSystem);
    }

}
