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
        redisMessagePublisher.publish(roomId, message, sender);
        return messagePayload;
    }

    private String extractSenderFromPayload(String payload) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> map = objectMapper.readValue(payload, new TypeReference<>() {
            });
            return map.get("sender");
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private String extractMessageFromPayload(String payload) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> map = objectMapper.readValue(payload, new TypeReference<>() {
            });
            return map.get("message");
        } catch (Exception e) {
            return "";
        }
    }
}
