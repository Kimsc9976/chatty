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
    private final ChatMessageController chatMessageController;

    @PostMapping("/join")
    public ResponseEntity<List<Map<String, String>>> joinChatRoom(@RequestParam Long chatRoomId, @RequestParam String userName) throws JsonProcessingException {
        System.out.println("joinChatRoom 호출됨");
        chatRoomMemberService.joinChatRoom(chatRoomId, userName);
        String chatMessage = userName + " 님이 입장하셨습니다.";

        // 채팅 메시지 발행
        chatMessageController.sendMessage(chatRoomId.toString(), new ObjectMapper().writeValueAsString(createResponse(chatMessage)));

        // 멤버 리스트 업데이트 후 발행
        updateMembersList(chatRoomId);

        // 두 개의 응답을 리스트에 담아 반환
        List<Map<String, String>> responses = List.of(createResponse(chatMessage), createMemberResponse(chatRoomMemberService.getChatRoomMemberNames(chatRoomId)));

        return ResponseEntity.ok(responses);
    }

    @PostMapping("/leave")
    public ResponseEntity<List<Map<String, String>>> leaveChatRoom(@RequestParam Long chatRoomId, @RequestParam String userName) throws JsonProcessingException {
        chatRoomMemberService.leaveChatRoom(chatRoomId, userName);
        String chatMessage = userName + " 님이 퇴장하셨습니다.";

        // 채팅 메시지 발행
        chatMessageController.sendMessage(chatRoomId.toString(), new ObjectMapper().writeValueAsString(createResponse(chatMessage)));

        // 멤버 리스트 업데이트 후 발행
        updateMembersList(chatRoomId);

        // 두 개의 응답을 리스트에 담아 반환
        List<Map<String, String>> responses = List.of(createResponse(chatMessage), createMemberResponse(chatRoomMemberService.getChatRoomMemberNames(chatRoomId)));

        return ResponseEntity.ok(responses);
    }

    private void updateMembersList(Long chatRoomId) {
        try {
            List<String> members = chatRoomMemberService.getChatRoomMemberNames(chatRoomId);
            String roomId = chatRoomId.toString();

            System.out.println("updateMembersList 호출됨, roomId: " + roomId + ", members: " + members);
            chatMessageController.updateMembersList(roomId, members);

        } catch (Exception e) {
            System.out.println("오류 메시지: " + e.getMessage());
        }
    }

    private Map<String, String> createResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("sender", "System");
        response.put("message", message);
        response.put("type", "chat");
        System.out.println("createResponse: " + response);
        return response;
    }

    private Map<String, String> createMemberResponse(List<String> members) {
        Map<String, String> response = new HashMap<>();
        response.put("sender", "System");
        response.put("message", members.toString());
        response.put("type", "members");
        System.out.println("createMemberResponse: " + response);
        return response;
    }
}
