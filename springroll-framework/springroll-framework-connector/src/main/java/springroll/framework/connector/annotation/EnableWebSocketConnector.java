package springroll.framework.connector.annotation;

import org.springframework.context.annotation.Import;
import springroll.framework.connector.config.WebSocketConnectorConfig;
import springroll.framework.core.AgentActor;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
@Import(WebSocketConnectorConfig.class)
public @interface EnableWebSocketConnector {

    Class<? extends AgentActor>[] value() default {};

}
