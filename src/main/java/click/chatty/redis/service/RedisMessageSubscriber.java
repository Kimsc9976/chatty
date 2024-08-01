package click.chatty.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(pattern, StandardCharsets.UTF_8);
        String roomId = extractRoomIdFromChannel(channel);
        String receivedMessage = new String(message.getBody(), StandardCharsets.UTF_8);

        if (channel.startsWith("members.")) {
            System.out.println("서버에서 클라이언트로 보내는 마지막 구간 메세지 : " + receivedMessage);
            messagingTemplate.convertAndSend("/sub/members/" + roomId, receivedMessage);
        } else if (channel.startsWith("chat.")) {
            messagingTemplate.convertAndSend("/sub/chat/" + roomId, receivedMessage);
        } else {
            System.out.println("알 수 없는 채널에서 메시지 수신: " + channel);
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
