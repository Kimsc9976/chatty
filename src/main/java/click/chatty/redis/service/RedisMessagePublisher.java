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

            // 멤버 리스트 메시지 발행
            if ("members".equals(type)) {
                messageMap.put("members", messageContent);
                jsonMessage = objectMapper.writeValueAsString(messageMap);
                System.out.println("멤버 리스트 토픽 : members." + roomId + " 토픽 메세지 : " + jsonMessage);
                redisTemplate.convertAndSend("members." + roomId, jsonMessage);
            }

            // 채팅 메시지 발행
            if ("chat".equals(type)) {
                messageMap.put("message", messageContent);
                jsonMessage = objectMapper.writeValueAsString(messageMap);
                System.out.println("채팅 토픽 : chat." + roomId + " 토픽 메세지 : " + jsonMessage);
                redisTemplate.convertAndSend("chat." + roomId, jsonMessage);
            }

        } catch (Exception e) {
            System.out.println("오류 메시지: " + e.getMessage());
        }
    }
}
