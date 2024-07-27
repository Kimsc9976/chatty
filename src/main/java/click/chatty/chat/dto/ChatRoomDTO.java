package click.chatty.chat.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatRoomDTO {

    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private int memberCount;

}
