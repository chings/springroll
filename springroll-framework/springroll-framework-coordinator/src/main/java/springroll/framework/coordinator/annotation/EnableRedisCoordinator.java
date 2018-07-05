package springroll.framework.coordinator.annotation;

import org.springframework.context.annotation.Import;
import springroll.framework.coordinator.RedisCoordinatorConfig;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
@Import(RedisCoordinatorConfig.class)
public @interface EnableRedisCoordinator { }
