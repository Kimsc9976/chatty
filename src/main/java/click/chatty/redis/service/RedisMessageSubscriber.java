package click.chatty.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(pattern);
        String roomId = channel.split("\\.")[1];
        String receivedMessage = message.toString();
        messagingTemplate.convertAndSend("/sub/chat/" + roomId, receivedMessage);
    }
}
