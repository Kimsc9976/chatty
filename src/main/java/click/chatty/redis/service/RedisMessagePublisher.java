package click.chatty.redis.service;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RedisMessagePublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(String roomId, String message) {
        System.out.println("Publishing message to room " + roomId + ": " + message);

        redisTemplate.convertAndSend("chat." + roomId, message);
    }
}
