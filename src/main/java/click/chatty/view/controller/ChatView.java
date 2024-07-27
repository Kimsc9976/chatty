package click.chatty.view.controller;

import click.chatty.chat.dto.ChatRoomDTO;
import click.chatty.chat.service.ChatRoomService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
@Slf4j
@Controller
@AllArgsConstructor
public class ChatView {

    private final ChatRoomService chatRoomService;

    @GetMapping("/")
    public String index(Model model) {
        log.info("MainView.index() 불려옴 =============================== {}", log.getName());
        log.debug("오류 발생 시 디버그 로그 확인용 =============================== {}", log.getName());

        // 채팅방 목록을 조회하여 모델에 추가
        List<ChatRoomDTO> chatRooms = chatRoomService.findAll();
        model.addAttribute("chatRooms", chatRooms);

        return "index";
    }

    @GetMapping("/chatroom/{id}")
    public String getChatRoom(@PathVariable Long id, Model model) {
        ChatRoomDTO chatRoom = chatRoomService.findById(id);
        model.addAttribute("chatRoom", chatRoom);
        return "view/chatroom";
    }
}
