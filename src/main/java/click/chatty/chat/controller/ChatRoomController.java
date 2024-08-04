package click.chatty.chat.controller;

import click.chatty.redis.controller.ChatMessageController;
import click.chatty.chat.service.ChatRoomMemberService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/chatroom")
public class ChatRoomController {

    private final ChatRoomMemberService chatRoomMemberService;
    private final ChatMessageController chatMessageController; // ChatMessageController 주입

    @PostMapping("/join")
    public ResponseEntity<Map<String, String>> joinChatRoom(@RequestParam Long chatRoomId, @RequestParam String userName) throws JsonProcessingException {
        chatRoomMemberService.joinChatRoom(chatRoomId, userName);
        String message = userName + " 님이 입장하셨습니다.";

        // ChatMessageController를 통해 메시지 발행
        chatMessageController.sendMessage(chatRoomId.toString(), new ObjectMapper().writeValueAsString(createResponse(message)));

        // 멤버 리스트 업데이트 후 발행
        updateMembersList(chatRoomId);

        return ResponseEntity.ok(createResponse(message));
    }

    @PostMapping("/leave")
    public ResponseEntity<Map<String, String>> leaveChatRoom(@RequestParam Long chatRoomId, @RequestParam String userName) throws JsonProcessingException {
        chatRoomMemberService.leaveChatRoom(chatRoomId, userName);
        String message = userName + " 님이 퇴장하셨습니다.";

        // ChatMessageController를 통해 메시지 발행
        chatMessageController.sendMessage(chatRoomId.toString(), new ObjectMapper().writeValueAsString(createResponse(message)));

        // 멤버 리스트 업데이트 후 발행
        updateMembersList(chatRoomId);

        return ResponseEntity.ok(createResponse(message));
    }

    private void updateMembersList(Long chatRoomId) {
        try {
            List<String> members = chatRoomMemberService.getChatRoomMemberNames(chatRoomId);
            String roomId = chatRoomId.toString();

            // 멤버 리스트를 "System" sender와 멤버 리스트 메시지를 포함한 형식으로 변환
            Map<String, Object> membersResponse = createMembersResponse(members);

            // JSON 문자열로 변환하여 발행
            String jsonMembersPayload = new ObjectMapper().writeValueAsString(membersResponse);
            System.out.println("멤버 리스트 업데이트 후 발행 chatRoomController : " + jsonMembersPayload);
            // ChatMessageController를 통해 멤버 리스트 업데이트 발행
            chatMessageController.updateMembersList(roomId, jsonMembersPayload);

        } catch (Exception e) {
            System.out.println("오류 메시지: " + e.getMessage());
        }
    }

    private Map<String, Object> createMembersResponse(List<String> members) {
        Map<String, Object> response = new HashMap<>();
        response.put("sender", "System");
        response.put("members", members);
        response.put("type", "members");
        return response;
    }

    private Map<String, String> createResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("sender", "System");
        response.put("message", message);
        response.put("type", "chat");
        return response;
    }
}
