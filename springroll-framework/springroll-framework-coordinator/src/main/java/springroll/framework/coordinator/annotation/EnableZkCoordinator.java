package springroll.framework.coordinator.annotation;

import org.springframework.context.annotation.Import;
import springroll.framework.coordinator.zk.config.ZkCoordinatorConfig;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
@Import(ZkCoordinatorConfig.class)
public @interface EnableZkCoordinator { }
