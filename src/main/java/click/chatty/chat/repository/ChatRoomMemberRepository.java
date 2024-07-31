package click.chatty.chat.repository;

import click.chatty.chat.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

        List<ChatRoomMember> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);
        int countByChatRoomId(Long chatRoomId);
        void deleteByChatRoomIdAndUserId(Long chatRoomId, Long userId);

        @Query("SELECT u.username FROM ChatRoomMember crm JOIN User u ON crm.userId = u.id WHERE crm.chatRoomId = :chatRoomId")
        List<String> findUsernamesByChatRoomId(Long chatRoomId);
}
