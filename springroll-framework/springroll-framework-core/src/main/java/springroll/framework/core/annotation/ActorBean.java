package springroll.framework.core.annotation;

import akka.actor.Actor;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
@Documented
public @interface ActorBean {

    String value() default "";

    Class<? extends Actor> actorClass() default Actor.class;

    String beanName() default "";

    String namespace() default "";

}
