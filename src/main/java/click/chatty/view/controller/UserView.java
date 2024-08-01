package click.chatty.view.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserView {

    @GetMapping("/register")
    public String join() {
        return "form/join";
    }

    @GetMapping("/logins")
    public String login() {
        return "form/login";
    }
}
