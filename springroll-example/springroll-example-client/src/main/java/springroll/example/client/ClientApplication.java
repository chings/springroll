package springroll.example.client;

import org.jline.utils.AttributedString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.jline.PromptProvider;
import springroll.framework.annotation.EnableActorSystem;

@SpringBootApplication
@EnableActorSystem
public class ClientApplication {

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

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ClientApplication.class, args);
    }

}
