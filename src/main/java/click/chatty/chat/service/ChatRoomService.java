package click.chatty.chat.service;

import click.chatty.chat.dto.ChatRoomDTO;
import click.chatty.chat.entity.ChatRoom;
import click.chatty.chat.repository.ChatRoomMemberRepository;
import click.chatty.chat.repository.ChatRoomRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberService chatRoomMemberService;

    public ChatRoomDTO createChatRoom(String name) {
        ChatRoom chatRoom = ChatRoom.builder()
                .name(name)
                .build();
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        return convertToDTO(savedChatRoom);
    }

    public List<ChatRoomDTO> findAll() {
        return chatRoomRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ChatRoomDTO findById(Long id) {
        return chatRoomRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    private ChatRoomDTO convertToDTO(ChatRoom chatRoom) {
        return ChatRoomDTO.builder()
                .id(chatRoom.getId())
                .name(chatRoom.getName())
                .createdAt(chatRoom.getCreatedAt())
                .memberCount(chatRoomMemberService.getChatRoomMemberCount(chatRoom.getId()))
                .build();
    }


}
