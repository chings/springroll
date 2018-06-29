package springroll.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class ServerApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ServerApplication.class, args);
    }

}
