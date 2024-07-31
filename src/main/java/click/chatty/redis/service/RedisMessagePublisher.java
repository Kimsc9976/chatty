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

    public void publish(String roomId, String message, String sender) {
        try {
            Map<String, String> messageMap = new HashMap<>();
            messageMap.put("message", message);
            messageMap.put("sender", sender);
            String jsonMessage = objectMapper.writeValueAsString(messageMap);
            redisTemplate.convertAndSend("chat." + roomId, jsonMessage);
        } catch (Exception e) {
            System.out.println("오류 메시지: " + e.getMessage());
        }
    }
}
