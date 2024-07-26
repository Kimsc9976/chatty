package click.chatty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"click.chatty", "click.global.config", "click.chatty.user"})

public class ChattyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChattyApplication.class, args);
    }

}
