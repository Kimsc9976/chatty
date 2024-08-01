package click.chatty.user.service;

import click.chatty.user.dto.UserDTO;
import click.chatty.user.entity.User;
import click.chatty.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void join(UserDTO userDTO) {
        User user = User.fromDTO(userDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        log.info("Join user: {}", user);
        userRepository.save(user);
    }

    public UserDTO findByUsername(String userName) {
        UserDTO userDTO = userRepository.findByUsername(userName)
                .map(User::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userName));
        log.info("Find user: {}", userDTO);
        return userDTO;
    }
}
