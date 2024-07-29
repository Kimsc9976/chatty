package click.chatty.redis.controller;

import click.chatty.redis.service.RedisMessagePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final RedisMessagePublisher redisMessagePublisher;

    @MessageMapping("/chat/{roomId}")
    @SendTo("/sub/chat/{roomId}")
    public String sendMessage(@DestinationVariable String roomId, String message) {
        System.out.println("메시지 전송: " + message + " to room: " + roomId);
        redisMessagePublisher.publish(roomId, message);
        return message;
    }
}
