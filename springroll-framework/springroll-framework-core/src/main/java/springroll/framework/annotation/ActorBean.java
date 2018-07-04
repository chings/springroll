package springroll.framework.annotation;

import akka.actor.Actor;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
@Documented
public @interface ActorBean {

    Class<? extends Actor> value();

    String name() default "";

}
