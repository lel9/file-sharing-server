package app;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@ComponentScan({"service", "app"})
@EntityScan("model")
@EnableJpaRepositories("repository")
public class Application {

    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringApplication.run(Application.class, args);
        Server server = context.getBean(Server.class);
        server.startServer();
    }
}