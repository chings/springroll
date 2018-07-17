package springroll.framework.core.util;

import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Map;
import java.util.function.BiConsumer;

public class Settings {

    public static void forEach(AbstractEnvironment environment, BiConsumer<String, Object> consumer) {
        for(PropertySource<?> propertySource : environment.getPropertySources()) {
            if(propertySource instanceof MapPropertySource) {
                Map<String, Object> source = ((MapPropertySource)propertySource).getSource();
                for(Map.Entry<String, Object> entry : source.entrySet()) {
                    consumer.accept(entry.getKey(), entry.getValue());
                }
            }
        }
    }

}
