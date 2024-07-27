package click.chatty.chat.service;

import click.chatty.chat.entity.ChatRoomMember;
import click.chatty.chat.repository.ChatRoomMemberRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ChatRoomMemberService {

    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @Transactional
    public ChatRoomMember joinChatRoom(Long chatRoomId, Long userId) {
        ChatRoomMember chatRoomMember = ChatRoomMember.builder()
                .chatRoomId(chatRoomId)
                .userId(userId)
                .build();
        return chatRoomMemberRepository.save(chatRoomMember);
    }

    @Transactional
    public void leaveChatRoom(Long chatRoomId, Long userId) {
        List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoomId(chatRoomId);
        for (ChatRoomMember member : members) {
            if (member.getUserId().equals(userId)) {
                chatRoomMemberRepository.delete(member);
                break;
            }
        }
    }

    public int getChatRoomMemberCount(Long chatRoomId) {
        return chatRoomMemberRepository.findByChatRoomId(chatRoomId).size();
    }

}
