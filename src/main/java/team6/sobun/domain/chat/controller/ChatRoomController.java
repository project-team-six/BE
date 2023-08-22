package team6.sobun.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import team6.sobun.domain.chat.dto.ChatRoom;
import team6.sobun.domain.chat.repository.RedisChatRepository;
import team6.sobun.domain.chat.service.ChatService;
import team6.sobun.global.security.UserDetailsImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "채팅방 관련 API", description = "채팅방 조회, 생성, 참가, 삭제, 나가기")
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/chat")
public class ChatRoomController {

    private final RedisChatRepository redisChatRepository;
    private final ChatService chatService;

    @Operation(summary = "전체 채팅방 조회")
    @GetMapping("/rooms")
    @ResponseBody
    public List<ChatRoom> room() {
        List<ChatRoom> chatRoom = redisChatRepository.findAllRoom();
        chatRoom.stream().forEach(room -> room.setUserCount(redisChatRepository.getUserCount(room.getRoomId())));
        return chatRoom;
    }
    @Operation(summary = "나의 채팅방 조회")
    @GetMapping("/rooms/user")
    public ResponseEntity<List<ChatRoom>> userRooms(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return chatService.getUserRooms(userDetails.getUser());
    }
    // 채팅방 생성
    @Operation(summary = "채팅방 생성")
    @PostMapping("/room")
    @ResponseBody
    public ChatRoom createRoom(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return chatService.createRoom(userDetails.getNickname());
    }
    @Operation(summary = "채팅 신청")
    @PostMapping("/room/start/{userId}")
    @ResponseBody
    public ChatRoom startChatRoom(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Long userId) {
        return chatService.startChatRoom(userDetails.getUser(),userId);
    }
    @Operation(summary = "채팅방 정보 조회")
    @GetMapping("/room/{roomId}")
    @ResponseBody
    public ChatRoom roomInfo(@PathVariable String roomId) {
        return redisChatRepository.findRoomById(roomId);
    }

    @Operation(summary = "채팅방에 유저 추가")
    @PostMapping("/room/{roomId}")
    @ResponseBody
    @Transactional
    public ChatRoom addParticipantToChatRoom(@PathVariable String roomId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return chatService.addParticipantToChatRoom(roomId,userDetails.getUser());
    }
    @Operation(summary = "채팅방 삭제")
    @DeleteMapping("/room/{roomId}")
    public ResponseEntity<String> deleteRoom(@PathVariable String roomId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return chatService.deleteRoom(roomId,userDetails.getUser());
    }

    @Operation(summary = "SockJS -> 이때 접속")
    @GetMapping("/room/enterA/{roomId}")
    @ResponseBody
    public Map<String, String> roomDetail(@PathVariable String roomId) {
        Map<String, String> response = new HashMap<>();
        response.put("roomId", roomId);
        log.info("채팅 방 입장 페이지로 이동: roomId={}", roomId);
        return response;
    }

    @Operation(summary = "서버 테스트용 템플릿")
    @GetMapping("/room")
    public String rooms() {
        return "chat/room";
    }
    @Operation(summary = "서버 테스트용 템플릿")
    @GetMapping("/room/enter/{roomId}")
    public String roomDetail(Model model, @PathVariable String roomId) {
        model.addAttribute("roomId", roomId);
        return "chat/roomdetail";
    }

}