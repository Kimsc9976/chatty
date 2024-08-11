package click.chatty.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(pattern, StandardCharsets.UTF_8);
        String roomId = extractRoomIdFromChannel(channel);
        String receivedMessage = new String(message.getBody(), StandardCharsets.UTF_8);

        try {
            JsonNode jsonNode = objectMapper.readTree(receivedMessage);

            if (channel.startsWith("members.")) {
                messagingTemplate.convertAndSend("/sub/members/" + roomId, jsonNode);
                System.out.println("멤버 리스트 rms: " + jsonNode);
            } else if (channel.startsWith("chat.")) {
                messagingTemplate.convertAndSend("/sub/chat/" + roomId, jsonNode);
                System.out.println("채팅 메세지 rms: " + jsonNode);
            } else {
                System.out.println("알 수 없는 채널에서 메시지 수신: " + channel);
            }
        } catch (Exception e) {
            System.out.println("메시지 파싱 오류: " + e.getMessage());
        }
    }

    private String extractRoomIdFromChannel(String channel) {
        String[] parts = channel.split("\\.");
        if (parts.length > 1) {
            return parts[1];
        } else {
            throw new IllegalArgumentException("Invalid channel format: " + channel);
        }
    }
}
