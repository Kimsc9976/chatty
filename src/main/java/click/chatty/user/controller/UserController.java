package click.chatty.user.controller;

import click.chatty.user.dto.UserDTO;
import click.chatty.user.entity.User;
import click.chatty.user.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/join")
    public String join(@RequestBody UserDTO userDTO) {
        userService.join(userDTO);
        return "Join user: " + userDTO;
    }

    @PostMapping("/login")
    public String login(@RequestBody UserDTO userDTO) {

        return "Login user: " + userDTO.getUsername();
    }

}
