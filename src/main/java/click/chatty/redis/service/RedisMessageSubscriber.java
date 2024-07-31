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

        System.out.println("Received message: " + receivedMessage + " on channel: " + channel);

        messagingTemplate.convertAndSend("/sub/chat/" + roomId + "/members", receivedMessage);
    }

    private String extractRoomIdFromChannel(String channel) {
        if (channel.contains(".")) {
            return channel.split("\\.")[1];
        }
        throw new IllegalArgumentException("Invalid channel format: " + channel);
    }
}
