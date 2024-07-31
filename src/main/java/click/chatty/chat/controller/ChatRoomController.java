package click.chatty.chat.controller;

import click.chatty.chat.service.ChatRoomMemberService;
import click.chatty.redis.service.RedisMessagePublisher;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/chatroom")
public class ChatRoomController {

    private final ChatRoomMemberService chatRoomMemberService;
    private final RedisMessagePublisher redisMessagePublisher;

    @PostMapping("/join")
    public ResponseEntity<Map<String, String>> joinChatRoom(@RequestParam Long chatRoomId, @RequestParam String userName) {
        chatRoomMemberService.joinChatRoom(chatRoomId, userName);
        String joinMessage = userName + " 님이 입장하셨습니다.";

        publishMessageAndUpdateMembers(chatRoomId, joinMessage, "System");

        Map<String, String> response = new HashMap<>();
        response.put("sender", "System");
        response.put("message", joinMessage);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/leave")
    public ResponseEntity<Map<String, String>> leaveChatRoom(@RequestParam Long chatRoomId, @RequestParam String userName) {
        chatRoomMemberService.leaveChatRoom(chatRoomId, userName);
        String leaveMessage = userName + " 님이 퇴장하셨습니다.";

        publishMessageAndUpdateMembers(chatRoomId, leaveMessage, "System");

        Map<String, String> response = new HashMap<>();
        response.put("sender", "System");
        response.put("message", leaveMessage);

        return ResponseEntity.ok(response);
    }

    // 중복된 로직을 처리하는 메서드
    private void publishMessageAndUpdateMembers(Long chatRoomId, String message, String sender) {
        redisMessagePublisher.publish(chatRoomId.toString(), message, sender);

        // 사용자 리스트를 업데이트
        List<String> members = chatRoomMemberService.getChatRoomMemberNames(chatRoomId);
        System.out.println("Publishing members list: " + members);
        redisMessagePublisher.publish(chatRoomId + "/members", members, sender);
    }
}
