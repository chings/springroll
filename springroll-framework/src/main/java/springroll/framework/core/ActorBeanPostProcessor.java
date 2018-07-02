package springroll.framework.core;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import springroll.framework.annotation.ActorBean;

import java.lang.reflect.Field;

public class ActorBeanPostProcessor implements BeanPostProcessor, Ordered {

    ActorSystem actorSystem;

    ActorRegistry actorRegistry;

    public ActorBeanPostProcessor(ActorSystem actorSystem, ActorRegistry actorRegistry) {
        this.actorSystem = actorSystem;
        this.actorRegistry = actorRegistry;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                if(!field.getType().isAssignableFrom(ActorRef.class)) return;
                ActorBean actorBean = field.getAnnotation(ActorBean.class);
                if(actorBean == null) return;
                Class<? extends Actor> actorClass = actorBean.value();
                String name = actorBean.name();
                ActorRef ref = StringUtils.hasText(name) ?
                        actorSystem.actorOf(Props.create(actorBean.value()), name) :
                        actorSystem.actorOf(Props.create(actorBean.value()));
                ReflectionUtils.makeAccessible(field);
                field.set(bean, ref);
                actorRegistry.register(ref, actorClass, name);
            }
        });
        return bean;
    }

    @Override
    public int getOrder() {
        return 1;
    }

}
