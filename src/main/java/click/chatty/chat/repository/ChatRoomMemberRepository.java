package click.chatty.chat.repository;

import click.chatty.chat.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

        List<ChatRoomMember> findByChatRoomId(Long chatRoomId);
}
