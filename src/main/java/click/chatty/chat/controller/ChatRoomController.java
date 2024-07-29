package click.chatty.chat.controller;

import click.chatty.chat.service.ChatRoomMemberService;
import click.chatty.redis.service.RedisMessagePublisher;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/chatroom")
public class ChatRoomController {

    private final ChatRoomMemberService chatRoomMemberService;
    private final RedisMessagePublisher redisMessagePublisher;

    @PostMapping("/join")
    public void joinChatRoom(@RequestParam Long chatRoomId, @RequestParam String userName) {
        chatRoomMemberService.joinChatRoom(chatRoomId, userName);
        String joinMessage = userName + " 님이 입장하셨습니다.";
        redisMessagePublisher.publish(chatRoomId.toString(), joinMessage);
    }

    @PostMapping("/leave")
    public void leaveChatRoom(@RequestParam Long chatRoomId, @RequestParam String userName) {
        chatRoomMemberService.leaveChatRoom(chatRoomId, userName);
        String leaveMessage = userName + " 님이 퇴장하셨습니다.";
        redisMessagePublisher.publish(chatRoomId.toString(), leaveMessage);
    }
}
