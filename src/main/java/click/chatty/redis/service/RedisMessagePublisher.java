package click.chatty.redis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class RedisMessagePublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(String roomId, Object messageContent, String sender, String type) {
        try {
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("sender", sender);
            messageMap.put("type", type);

            String jsonMessage;
            switch (type) {
                case "members":
                    jsonMessage = (String) messageContent;
                    System.out.println("멤버 리스트 토픽 : members." + roomId + " 토픽 메세지 : " + jsonMessage);
                    break;
                case "chat":
                    // chat 타입인 경우 기존 로직대로 직렬화
                    messageMap.put("message", messageContent);
                    jsonMessage = objectMapper.writeValueAsString(messageMap);
                    System.out.println("채팅 토픽 : chat." + roomId + " 토픽 메세지 : " + jsonMessage);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid message type: " + type);
            }

            // 메시지 발행
            redisTemplate.convertAndSend(type + "." + roomId, jsonMessage);

        } catch (Exception e) {
            System.out.println("오류 메시지: " + e.getMessage());
        }
    }


}
