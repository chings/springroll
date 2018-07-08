package springroll.framework.coordinator.zk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springroll.framework.coordinator.zk.ZkCoordinator;

@Configuration
public class ZkCoordinatorConfig {

    @Bean
    @ConfigurationProperties("springroll.coordination.zk")
    public ZkCoordinator zkCoordinator() {
        return new ZkCoordinator();
    }

}
