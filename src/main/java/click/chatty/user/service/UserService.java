package click.chatty.user.service;

import click.chatty.user.dto.UserDTO;
import click.chatty.user.entity.User;
import click.chatty.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    public void join(UserDTO userDTO) {
        User user = User.builder()
                .username(userDTO.getUsername())
                .password(userDTO.getPassword())
                .email(userDTO.getEmail())
                .role(User.Role.ROLE_USER)
                .build();
        log.info("Join user: {}", user);
        userRepository.save(user);
    }

}
