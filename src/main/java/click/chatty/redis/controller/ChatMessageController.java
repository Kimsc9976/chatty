package click.chatty.redis.controller;

import click.chatty.redis.service.RedisMessagePublisher;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatMessageController {

    private final RedisMessagePublisher redisMessagePublisher;
    private final ObjectMapper objectMapper;

    @MessageMapping("/chat/{roomId}")
    @SendTo("/sub/chat/{roomId}")
    public String sendMessage(@DestinationVariable String roomId, @Payload String messagePayload) {
        String uniqueId = generateUniqueId();
        log.info("[{}] sendMessage 호출됨, roomId: {}, messagePayload: {}", uniqueId, roomId, messagePayload);
        redisMessagePublisher.publish(roomId, extractMessageFromPayload(messagePayload), extractSenderFromPayload(messagePayload), "chat");
        return messagePayload;
    }

    @MessageMapping("/members/{roomId}")
    @SendTo("/sub/members/{roomId}")
    public List<String> updateMembersList(@DestinationVariable String roomId, @Payload List<String> members) {
        String uniqueId = generateUniqueId();
        log.info("[{}] updateMembersList 호출됨, roomId: {}, members: {}", uniqueId, roomId, members);
        redisMessagePublisher.publish(roomId, members.toString(), "System", "members");
        return members;
    }

    private String extractSenderFromPayload(String payload) {
        try {
            Map<String, String> map = objectMapper.readValue(payload, new TypeReference<>() {});
            String sender = map.get("sender");
            log.debug("[{}] 추출된 sender: {}", generateUniqueId(), sender);
            return sender;
        } catch (Exception e) {
            log.warn("[{}] sender 추출 오류: {}", generateUniqueId(), e.getMessage(), e);
            return "Unknown";
        }
    }

    private String extractMessageFromPayload(String payload) {
        try {
            Map<String, String> map = objectMapper.readValue(payload, new TypeReference<>() {});
            String message = map.get("message");
            log.debug("[{}] 추출된 message: {}", generateUniqueId(), message);
            return message;
        } catch (Exception e) {
            log.warn("[{}] message 추출 오류: {}", generateUniqueId(), e.getMessage(), e);
            return "";
        }
    }

    private String generateUniqueId() {
        return java.util.UUID.randomUUID().toString();
    }
}
