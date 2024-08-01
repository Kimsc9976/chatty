package click.chatty.chat.service;

import click.chatty.chat.entity.ChatRoomMember;
import click.chatty.chat.repository.ChatRoomMemberRepository;
import click.chatty.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ChatRoomMemberService {

    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserService userService;

    @Transactional
    public void joinChatRoom(Long chatRoomId, String userName) {
        Long userId = userService.findByUsername(userName).getId();
        List<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findByChatRoomIdAndUserId(chatRoomId, userId);
        if (chatRoomMembers.isEmpty()) {
            ChatRoomMember chatRoomMember = ChatRoomMember.builder()
                    .chatRoomId(chatRoomId)
                    .userId(userId)
                    .build();
            chatRoomMemberRepository.save(chatRoomMember);
        }
    }

    @Transactional
    public void leaveChatRoom(Long chatRoomId, String userName) {
        Long userId = userService.findByUsername(userName).getId();
        chatRoomMemberRepository.deleteByChatRoomIdAndUserId(chatRoomId, userId);
    }


    public int getChatRoomMemberCount(Long id) {
        return chatRoomMemberRepository.countByChatRoomId(id);
    }

    public List<String> getChatRoomMemberNames(Long chatRoomId) {
        System.out.println("================================");
        System.out.println("방 채널: " + chatRoomId);
        System.out.println("참여 인원 이름 : " + chatRoomMemberRepository.findUsernamesByChatRoomId(chatRoomId));
        System.out.println("===================================");
        return chatRoomMemberRepository.findUsernamesByChatRoomId(chatRoomId);
    }
}
