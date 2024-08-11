package click.chatty.redis.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class RedisMessagePublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    private final Map<String, Long> messageTimestamps = new HashMap<>();
    private static final long DUPLICATE_THRESHOLD_MS = 500; // 0.5초

    public void publish(String roomId, Object messageContent, String sender, String type) {
        String uniqueId = generateUniqueId();
        String channel = type + "." + roomId;
        String messageKey = channel + ":" + messageContent.hashCode();
        long currentTime = System.currentTimeMillis();

        // 중복 메시지 확인
        if (messageTimestamps.containsKey(messageKey)) {
            long lastTimestamp = messageTimestamps.get(messageKey);
            if ((currentTime - lastTimestamp) < DUPLICATE_THRESHOLD_MS) {
                log.info("[{}] 중복 메시지 발견, 무시됨: {}", uniqueId, messageContent);
                return;
            }
        }

        try {
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("sender", sender);
            messageMap.put("type", type);
            messageMap.put("message", messageContent);

            log.info("[{}] publish 호출됨, roomId: {}, messageContent: {}, sender: {}, type: {}", uniqueId, roomId, messageContent, sender, type);
            redisTemplate.convertAndSend(channel, messageMap);
            log.info("[{}] 메시지 발행됨, 채널: {}, 메시지: {}", uniqueId, channel, messageMap);

            // 메시지 타임스탬프 기록
            messageTimestamps.put(messageKey, currentTime);

        } catch (Exception e) {
            log.error("[{}] 메시지 발행 오류: {}", uniqueId, e.getMessage(), e);
        }
    }

    private String generateUniqueId() {
        return java.util.UUID.randomUUID().toString();
    }
}
