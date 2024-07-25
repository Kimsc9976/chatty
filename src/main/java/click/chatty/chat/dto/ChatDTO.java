package click.chatty.chat.dto;

import lombok.Data;

@Data
public class ChatDTO {

    private Long id;
    private String message;
    private String sender;
    private String receiver;
    private String timestamp;

}
