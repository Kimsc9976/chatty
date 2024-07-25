package click.chatty.chat.entity;

import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import lombok.*;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String message;
    private String sender;
    private String receiver;
    private String timestamp;



}
