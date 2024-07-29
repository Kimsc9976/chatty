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

        System.out.println("====== 메시지 전송 ======");
        System.out.println("메시지: " + message + ", 방 ID: " + roomId + ", 보낸 사람: " + sender);
        System.out.println("================================");

        redisMessagePublisher.publish(roomId, message, sender);
        return messagePayload; // 전체 payload를 반환
    }

    private String extractSenderFromPayload(String payload) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> map = objectMapper.readValue(payload, new TypeReference<Map<String, String>>() {});
            return map.get("sender");
        } catch (Exception e) {
            System.out.println("====== 메시지 발송 중 오류 발생 ======");
            System.out.println("오류 메시지: " + e.getMessage());
            System.out.println("================================");
            return "Unknown";
        }
    }

    private String extractMessageFromPayload(String payload) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> map = objectMapper.readValue(payload, new TypeReference<Map<String, String>>() {});
            return map.get("message");
        } catch (Exception e) {
            System.out.println("====== 메시지 발송 중 오류 발생 ======");
            System.out.println("오류 메시지: " + e.getMessage());
            System.out.println("================================");
            return "";
        }
    }
}
