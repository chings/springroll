package springroll.framework.annotation;

import akka.actor.Actor;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
@Documented
public @interface ActorReference {

    Class<? extends Actor> value() default Actor.class;

    String path() default "";

}
