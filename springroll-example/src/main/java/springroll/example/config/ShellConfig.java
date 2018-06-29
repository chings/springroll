package springroll.example.config;

import org.jline.utils.AttributedString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.jline.PromptProvider;

@Configuration
public class ShellConfig {

    @Value("${application.name}")
    String applicationName;

    @Bean
    public PromptProvider promptProvider() {
        return new PromptProvider() {
            @Override
            public AttributedString getPrompt() {
                return new AttributedString(applicationName + ">");
            }
        };
    }

}
