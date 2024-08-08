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
            messageMap.put("message", messageContent);

            String jsonMessage = objectMapper.writeValueAsString(messageMap);
            redisTemplate.convertAndSend(type + "." + roomId, jsonMessage);

        } catch (Exception e) {
            System.out.println("오류 메시지: " + e.getMessage());
        }
    }

}
