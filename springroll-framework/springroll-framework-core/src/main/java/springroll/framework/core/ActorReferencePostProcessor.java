package springroll.framework.core;

import akka.actor.ActorRef;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import springroll.framework.annotation.ActorReference;

public class ActorReferencePostProcessor implements BeanPostProcessor, Ordered {

    ActorRegistry actorRegistry;

    public ActorReferencePostProcessor(ActorRegistry actorRegistry) {
        this.actorRegistry = actorRegistry;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithFields(bean.getClass(), field -> {
            if(!field.getType().isAssignableFrom(ActorRef.class)) return;
            ActorReference actorReference = field.getAnnotation(ActorReference.class);
            if(actorReference == null) return;
            String actorPath = actorReference.value();
            ActorRef ref = actorRegistry.find(actorPath);
            if(ref == null) throw new NoSuchBeanDefinitionException(actorPath);
            if(!field.isAccessible()) field.setAccessible(true);
            field.set(bean, ref);
        });
        return bean;
    }

    @Override
    public int getOrder() {
        return 2;
    }

}
