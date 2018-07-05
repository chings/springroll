package springroll.framework.core;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import springroll.framework.annotation.ActorReference;

public class ActorReferencePostProcessor implements BeanPostProcessor, Ordered {
    private static Logger log = LoggerFactory.getLogger(ActorReferencePostProcessor.class);

    ActorRegistry actorRegistry;

    public ActorReferencePostProcessor(ActorRegistry actorRegistry) {
        this.actorRegistry = actorRegistry;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithFields(bean.getClass(), field -> {
            ActorReference actorReference = field.getAnnotation(ActorReference.class);
            if(actorReference == null) return;
            if(!field.getType().isAssignableFrom(ActorSelection.class) || !field.getType().isAssignableFrom(ActorRef.class))
                throw new NotWritablePropertyException(bean.getClass(), field.getName(), "only ActorSelection/ActorRef can be injected by @ActorReference");
            String actorPath = actorReference.value();
            if(!actorPath.startsWith("/user")) actorPath += actorPath.startsWith("/") ? "/user" + actorPath : "/user/" + actorPath;
            Object value = field.getType().isAssignableFrom(ActorSelection.class) ? actorRegistry.select(actorPath) : actorRegistry.get(actorPath);
            if(value == null) throw new NoSuchBeanDefinitionException(actorPath);
            if(!field.isAccessible()) field.setAccessible(true);
            field.set(bean, value);
        });
        return bean;
    }

    @Override
    public int getOrder() {
        return 2;
    }

}