package click.chatty.redis.controller;

import click.chatty.redis.service.RedisMessagePublisher;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final RedisMessagePublisher redisMessagePublisher;

    @MessageMapping("/chat/{roomId}")
    @SendTo("/sub/chat/{roomId}")
    public String sendMessage(@DestinationVariable String roomId, @Payload String messagePayload) {
        String sender = extractSenderFromPayload(messagePayload);
        String message = extractMessageFromPayload(messagePayload);
        redisMessagePublisher.publish(roomId, message, sender, "chat");
        System.out.println("메시지 발행: " + messagePayload);
        return messagePayload;
    }

    @MessageMapping("/members/{roomId}")
    @SendTo("/sub/members/{roomId}")
    public List<String> updateMembersList(@DestinationVariable String roomId, @Payload List<String> members) {
        try {
            redisMessagePublisher.publish(roomId, members, "System", "members");
            return members;

        } catch (Exception e) {
            System.out.println("오류 메시지: " + e.getMessage());
            return null;
        }
    }



    private String extractSenderFromPayload(String payload) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> map = objectMapper.readValue(payload, new TypeReference<>() {});
            return map.get("sender");
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private String extractMessageFromPayload(String payload) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> map = objectMapper.readValue(payload, new TypeReference<>() {});
            return map.get("message");
        } catch (Exception e) {
            return "";
        }
    }
}
