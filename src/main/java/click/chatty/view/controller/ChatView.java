package click.chatty.view.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatView {

    @GetMapping("/chat")
    public String chat() {
        return "view/chatroom";
    }
}
