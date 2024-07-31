package click.chatty.chat.controller;

import click.chatty.chat.service.ChatRoomMemberService;
import click.chatty.redis.service.RedisMessagePublisher;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
        redisMessagePublisher.publish(chatRoomId.toString(), joinMessage, "System");

        Map<String, String> response = new HashMap<>();
        response.put("sender", "System");
        response.put("message", joinMessage);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/leave")
    public ResponseEntity<Map<String, String>> leaveChatRoom(@RequestParam Long chatRoomId, @RequestParam String userName) {
        chatRoomMemberService.leaveChatRoom(chatRoomId, userName);
        String leaveMessage = userName + " 님이 퇴장하셨습니다.";
        redisMessagePublisher.publish(chatRoomId.toString(), leaveMessage, "System");

        Map<String, String> response = new HashMap<>();
        response.put("sender", "System");
        response.put("message", leaveMessage);

        return ResponseEntity.ok(response);
    }
}
