package springroll.framework.core;

import akka.actor.Actor;
import akka.actor.ActorRef;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import springroll.framework.annotation.ActorReference;

import java.lang.reflect.Field;

public class ActorReferencePostProcessor implements BeanPostProcessor, Ordered {

    ActorRegistry actorRegistry;

    public ActorReferencePostProcessor(ActorRegistry actorRegistry) {
        this.actorRegistry = actorRegistry;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                if(!field.getType().isAssignableFrom(ActorRef.class)) return;
                ActorReference actorReference = field.getAnnotation(ActorReference.class);
                if(actorReference == null) return;
                Class<? extends Actor> actorClass = actorReference.value();
                ActorRef ref = actorRegistry.findByClass(actorClass);
                if(ref == null) throw new NoSuchBeanDefinitionException(ActorRef.class);
                ReflectionUtils.makeAccessible(field);
                field.set(bean, ref);
            }
        });
        return bean;
    }

    @Override
    public int getOrder() {
        return 2;
    }

}
