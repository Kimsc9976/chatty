package click.chatty.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "social_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SocialUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "enum('GOOGLE', 'KAKAO')")
    private Provider provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    public enum Provider {
        GOOGLE,
        KAKAO
    }

}
