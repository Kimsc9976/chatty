package click.chatty.view.controller;

import click.chatty.user.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
public class MainView {

    @GetMapping("/")
    public String index(Model model) {
        log.info("MainView.index() 불려옴 =============================== {}", log.getName());
        log.debug("오류 발생 시 디버그 로그 확인용 =============================== {}", log.getName());


        // TODO : Authenticate principal 정보를 가져 와서 model에 담아서 전달 (로그인 정보)
        return "index";
    }

}
