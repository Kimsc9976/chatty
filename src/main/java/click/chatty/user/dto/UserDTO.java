package click.chatty.user.dto;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder
public class UserDTO {

    private Long id;
    private String username;
    private String password;
    private String email;

}
