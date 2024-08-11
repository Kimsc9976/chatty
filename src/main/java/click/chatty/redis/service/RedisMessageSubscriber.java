package click.chatty.redis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisMessageSubscriber implements MessageListener {

    private static final String CHAT_CHANNEL_PREFIX = "chat.";
    private static final String MEMBERS_CHANNEL_PREFIX = "members.";
    private static final String SUB_CHAT_PREFIX = "/sub/chat/";
    private static final String SUB_MEMBERS_PREFIX = "/sub/members/";

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    private final Map<String, Long> messageTimestamps = new HashMap<>();
    private static final long DUPLICATE_THRESHOLD_MS = 500; // 중복 허용 시간 (0.5초)
    private static final long CLEANUP_INTERVAL_MS = 60000; // 메모리 청소 주기 (1분)

    private long lastCleanupTime = System.currentTimeMillis();

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(pattern, StandardCharsets.UTF_8);
        String roomId = extractRoomIdFromChannel(channel);
        String receivedMessage = new String(message.getBody(), StandardCharsets.UTF_8);

        log.info("수신된 메시지 - 채널: {}, 방 ID: {}, 메시지: {}", channel, roomId, receivedMessage);

        try {
            JsonNode jsonNode = objectMapper.readTree(receivedMessage);
            String messageKey = generateMessageKey(channel, receivedMessage);
            long currentTime = System.currentTimeMillis();

            // 메모리 청소
            if (currentTime - lastCleanupTime > CLEANUP_INTERVAL_MS) {
                log.debug("메모리 청소 - 현재 시간: {}, 마지막 청소 시간: {}", currentTime, lastCleanupTime);
                cleanupOldEntries(currentTime);
                lastCleanupTime = currentTime;
            }

            // 중복 메시지 확인
            if (messageTimestamps.containsKey(messageKey)) {
                long lastTimestamp = messageTimestamps.get(messageKey);
                if ((currentTime - lastTimestamp) < DUPLICATE_THRESHOLD_MS) {
                    log.info("중복 메시지 발견, 무시됨: {}", receivedMessage);
                    return;
                }
            }

            // 메시지 처리 및 발행
            if (channel.startsWith(MEMBERS_CHANNEL_PREFIX)) {
                log.info("멤버 리스트 메시지 발행 - 채널: {}, 방 ID: {}, 내용: {}", channel, roomId, jsonNode);
                messagingTemplate.convertAndSend(SUB_MEMBERS_PREFIX + roomId, jsonNode);
            } else if (channel.startsWith(CHAT_CHANNEL_PREFIX)) {
                log.info("채팅 메시지 발행 - 채널: {}, 방 ID: {}, 내용: {}", channel, roomId, jsonNode);
                messagingTemplate.convertAndSend(SUB_CHAT_PREFIX + roomId, jsonNode);
            } else {
                log.warn("알 수 없는 채널에서 메시지 수신: {}", channel);
            }

            // 메시지 타임스탬프 기록
            messageTimestamps.put(messageKey, currentTime);
            log.debug("메시지 타임스탬프 기록 - 키: {}, 타임스탬프: {}", messageKey, currentTime);

        } catch (Exception e) {
            log.error("메시지 파싱 오류: {}", e.getMessage(), e);
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

    private String generateMessageKey(String channel, String message) {
        return channel + ":" + message;
    }

    private void cleanupOldEntries(long currentTime) {
        log.debug("구형 항목 정리 시작 - 현재 시간: {}, 삭제 임계값: {}", currentTime, DUPLICATE_THRESHOLD_MS);
        messageTimestamps.entrySet().removeIf(entry -> (currentTime - entry.getValue()) >= DUPLICATE_THRESHOLD_MS);
        log.debug("구형 항목 정리 완료 - 현재 저장된 항목 수: {}", messageTimestamps.size());
    }
}
