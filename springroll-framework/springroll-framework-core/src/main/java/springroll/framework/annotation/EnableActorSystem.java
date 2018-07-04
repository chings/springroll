package springroll.framework.annotation;

import org.springframework.context.annotation.Import;
import springroll.framework.config.ActorSystemConfig;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
@Import(ActorSystemConfig.class)
public @interface EnableActorSystem { }
