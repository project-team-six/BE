package team6.sobun.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team6.sobun.domain.chat.dto.ChatMessage;
import team6.sobun.domain.chat.dto.ChatRoom;
import team6.sobun.domain.chat.service.ChatService;
import team6.sobun.global.security.UserDetailsImpl;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    public ChatRoom createRoom(@RequestBody String name, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // 현재 사용자 이름을 채팅방 생성 시 참가자로 전달합니다.
        return chatService.createRoom(name, userDetails.getUsername());
    }

    @PostMapping("/{roomId}/send")
    public void sendMessage(@PathVariable String roomId, @RequestBody ChatMessage chatMessage, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // 현재 사용자 이름을 채팅 메시지의 발신자(sender)로 설정합니다.
        chatMessage.setSender(userDetails.getUsername());
        chatMessage.setRoomId(roomId);

        // 채팅방에 있는 모든 세션들에게 메시지를 전송합니다.
        ChatRoom chatRoom = chatService.findRoomById(roomId);
        if (chatRoom != null) {
            chatRoom.handlerActions(null, chatMessage, chatService);
        }
    }

    @PostMapping("/{roomId}/leave")
    public void leaveRoom(@PathVariable String roomId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // 채팅방에서 사용자를 제거합니다.
        ChatRoom chatRoom = chatService.findRoomById(roomId);
        if (chatRoom != null) {
            chatRoom.removeParticipant(userDetails.getUsername());
        }
    }
}
