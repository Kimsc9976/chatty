package click.chatty.chat.controller;

import click.chatty.redis.controller.ChatMessageController;
import click.chatty.chat.service.ChatRoomMemberService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatroom")
@Slf4j
public class ChatRoomController {

    private final ChatRoomMemberService chatRoomMemberService;
    private final ChatMessageController chatMessageController;
    private final ObjectMapper objectMapper;

    @PostMapping("/join")
    public ResponseEntity<List<Map<String, String>>> joinChatRoom(@RequestParam Long chatRoomId, @RequestParam String userName) {
        String uniqueId = generateUniqueId();
        log.info("[{}] joinChatRoom 호출됨, chatRoomId: {}, userName: {}", uniqueId, chatRoomId, userName);

        chatRoomMemberService.joinChatRoom(chatRoomId, userName);
        String chatMessage = userName + " 님이 입장하셨습니다.";

        try {
            // 채팅 메시지 발행
            log.info("[{}] joinChatRoom 채팅 메시지 발행 중, 메시지: {}", uniqueId, chatMessage);
            chatMessageController.sendMessage(chatRoomId.toString(), objectMapper.writeValueAsString(createResponse(chatMessage)));

            // 멤버 리스트 업데이트 후 발행
            updateMembersList(chatRoomId);
        } catch (JsonProcessingException e) {
            log.error("[{}] JSON 처리 오류: {}", uniqueId, e.getMessage(), e);
        }

        return ResponseEntity.ok(List.of(createResponse(chatMessage), createMemberResponse(chatRoomMemberService.getChatRoomMemberNames(chatRoomId))));
    }

    @PostMapping("/leave")
    public ResponseEntity<List<Map<String, String>>> leaveChatRoom(@RequestParam Long chatRoomId, @RequestParam String userName) {
        String uniqueId = generateUniqueId();
        log.info("[{}] leaveChatRoom 호출됨, chatRoomId: {}, userName: {}", uniqueId, chatRoomId, userName);

        chatRoomMemberService.leaveChatRoom(chatRoomId, userName);
        String chatMessage = userName + " 님이 퇴장하셨습니다.";

        try {
            // 채팅 메시지 발행
            log.info("[{}] leaveChatRoom 채팅 메시지 발행 중, 메시지: {}", uniqueId, chatMessage);
            chatMessageController.sendMessage(chatRoomId.toString(), objectMapper.writeValueAsString(createResponse(chatMessage)));

            // 멤버 리스트 업데이트 후 발행
            updateMembersList(chatRoomId);
        } catch (JsonProcessingException e) {
            log.error("[{}] JSON 처리 오류: {}", uniqueId, e.getMessage(), e);
        }

        return ResponseEntity.ok(List.of(createResponse(chatMessage), createMemberResponse(chatRoomMemberService.getChatRoomMemberNames(chatRoomId))));
    }

    private void updateMembersList(Long chatRoomId) {
        String uniqueId = generateUniqueId();
        try {
            List<String> members = chatRoomMemberService.getChatRoomMemberNames(chatRoomId);
            log.info("[{}] updateMembersList 호출됨, roomId: {}, members: {}", uniqueId, chatRoomId, members);
            chatMessageController.updateMembersList(chatRoomId.toString(), members);
        } catch (Exception e) {
            log.error("[{}] 멤버 리스트 업데이트 오류: {}", uniqueId, e.getMessage(), e);
        }
    }

    private Map<String, String> createResponse(String message) {
        String uniqueId = generateUniqueId();
        Map<String, String> response = new HashMap<>();
        response.put("sender", "System");
        response.put("message", message);
        response.put("type", "chat");
        log.info("[{}] createResponse: {}", uniqueId, response);
        return response;
    }

    private Map<String, String> createMemberResponse(List<String> members) {
        String uniqueId = generateUniqueId();
        Map<String, String> response = new HashMap<>();
        response.put("sender", "System");
        response.put("message", members.toString());
        response.put("type", "members");
        log.info("[{}] createMemberResponse: {}", uniqueId, response);
        return response;
    }

    private String generateUniqueId() {
        return java.util.UUID.randomUUID().toString();
    }
}
