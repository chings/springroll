package springroll.framework.core;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import springroll.framework.annotation.ActorBean;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class ActorBeanPostProcessor implements ApplicationContextAware, BeanPostProcessor, Ordered {

    ActorSystem actorSystem;
    ActorRegistry actorRegistry;

    ApplicationContext applicationContext;

    public ActorBeanPostProcessor(ActorSystem actorSystem, ActorRegistry actorRegistry) {
        this.actorSystem = actorSystem;
        this.actorRegistry = actorRegistry;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithFields(bean.getClass(), field -> {
            if(!field.getType().isAssignableFrom(ActorRef.class)) return;
            ActorBean actorBean = field.getAnnotation(ActorBean.class);
            if(actorBean == null) return;
            Class<? extends Actor> actorClass = actorBean.value();
            String name = actorBean.name();
            if(StringUtils.isEmpty(name)) name = actorClass.getSimpleName();
            Object[] args = wireConstructorArgs(actorClass, applicationContext);
            ActorRef ref = actorSystem.actorOf(Props.create(actorClass, args), name);
            if(!field.isAccessible()) field.setAccessible(true);
            field.set(bean, ref);
            actorRegistry.register(ref);
        });
        return bean;
    }

    public Object[] wireConstructorArgs(Class<?> actorClass, BeanFactory factory) {
        List<Object> args = new ArrayList<>();
        Constructor<?>[] constructors = actorClass.getConstructors();
        if(constructors.length > 0) {
            for(Constructor constructor : constructors) {
                Class<?>[] argClasses = constructor.getParameterTypes();
                if(argClasses.length == 0) continue;
                args.clear();
                boolean matched = true;
                for(Class<?> argClass : argClasses) {
                    Object arg = factory.getBean(argClass);
                    if(arg == null) {
                        matched = false;
                        break;
                    }
                    args.add(arg);
                }
                if(matched) break;
            }
        }
        return args.toArray();
    }

    @Override
    public int getOrder() {
        return 1;
    }

}
