package springroll.framework.core;

import akka.actor.Actor;
import akka.actor.ActorRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import springroll.framework.core.annotation.ActorBean;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class ActorBeanPostProcessor implements ApplicationContextAware, BeanPostProcessor, Ordered {
    private static Logger log = LoggerFactory.getLogger(ActorBeanPostProcessor.class);

    ApplicationContext applicationContext;

    @Autowired
    SpringActorSystem springActorSystem;

    @Autowired
    ActorRegistry actorRegistry;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithFields(bean.getClass(), field -> {
            ActorBean actorBean = field.getAnnotation(ActorBean.class);
            if(actorBean == null) return;
            if(!field.getType().isAssignableFrom(ActorRef.class)) {
                throw new NotWritablePropertyException(bean.getClass(), field.getName(), "only ActorRef can be injected by @ActorReference");
            }

            String actorName = actorBean.value();
            Class<? extends Actor> actorClass = actorBean.actorClass();
            String actorBeanName = actorBean.beanName();
            if(actorClass != Actor.class) {
                if(StringUtils.isEmpty(actorName)) actorName = actorClass.getSimpleName();
            } else {
                if(StringUtils.isEmpty(actorBeanName)) actorBeanName = actorName;
                if(StringUtils.isEmpty(actorName)) actorName = actorBeanName;
            }

            ActorRef actorRef = null;
            if(actorClass != Actor.class) {
                //try create directly
                Object[] args = wireConstructorArgs(actorClass, applicationContext);
                actorRef = springActorSystem.spawn(actorName, actorClass, args);
            } else if(StringUtils.hasText(actorBeanName) && applicationContext.containsBean(actorBeanName)) {
                //try create from prototype Bean
                actorRef = springActorSystem.spawn(actorName, actorBeanName);
                actorClass = (Class<? extends Actor>)applicationContext.getType(actorBeanName);
            }
            if(actorRef == null) {
                throw new BeanCreationException("failed creating actor bean of " + actorClass.getCanonicalName());
            }
            String namespace = actorBean.namespace().isEmpty() ? actorClass.getPackage().getName() : actorBean.namespace();
            actorRegistry.register(actorRef, namespace);

            if(!field.isAccessible()) field.setAccessible(true);
            field.set(bean, actorRef);
        });
        return bean;
    }

    public static Object[] wireConstructorArgs(Class<? extends Actor> actorClass, BeanFactory factory) {
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
