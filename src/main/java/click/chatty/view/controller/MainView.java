package click.chatty.view.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
public class MainView {

    @GetMapping("/")
    public String index() {
        log.info("MainView.index() 불려옴 =============================== ");
        return "index";
    }

}
