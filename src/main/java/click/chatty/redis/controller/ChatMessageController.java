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
    public String updateMembersList(@DestinationVariable String roomId, @Payload String membersPayload) {
        try {
            // JSON 파싱하여 멤버 리스트 추출
            List<String> members = extractMembersFromPayload(membersPayload);
            // 멤버 리스트를 JSON 문자열로 변환하여 Redis에 발행
            String updatedMembersPayload = new ObjectMapper().writeValueAsString(Map.of("members", members));
            redisMessagePublisher.publish(roomId, updatedMembersPayload, "System", "members");
            System.out.println("멤버 리스트 발행 : " + updatedMembersPayload);
            return updatedMembersPayload;

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

    private List<String> extractMembersFromPayload(String payload) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> map = objectMapper.readValue(payload, new TypeReference<>() {});
            System.out.println("==========멤버 리스트 추출=========== : " + map.get("members"));
            return (List<String>) map.get("members");
        } catch (Exception e) {
            return List.of();
        }
    }
}
