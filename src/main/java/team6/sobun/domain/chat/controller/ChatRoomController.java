package team6.sobun.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import team6.sobun.domain.chat.dto.ChatRoom;
import team6.sobun.domain.chat.repository.ChatRoomRepository;
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
    private final ChatRoomRepository chatRoomRepository;
    private final ChatService chatService;

    @Operation(summary = "전체 채팅방 조회 -> 테스트 용도 ")
    @GetMapping("/rooms")
    @ResponseBody
    public List<ChatRoom> room() {
        List<ChatRoom> chatRoom = redisChatRepository.findAllRoom();
        chatRoom.stream().forEach(room -> room.setUserCount(redisChatRepository.getUserCount(room.getRoomId())));
        return chatRoom;
    }
    // 채팅방 생성
    @Operation(summary = "채팅방 생성 -> 테스트 용도 -> 게시물 작성시 채팅방은 자동생성")
    @PostMapping("/room")
    @ResponseBody
    public ChatRoom createRoom(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return chatService.createRoom(userDetails.getNickname());
    }
    @Operation(summary = "채팅방 정보 조회 -> 이걸로 접속 -> 이거 쓰세요")
    @GetMapping("/room/{roomId}")
    @ResponseBody
    public ChatRoom roomInfo(@PathVariable String roomId) {
        return redisChatRepository.findRoomById(roomId);
    }

    @Operation(summary = "나의 채팅방 조회 -> 이거 쓰세요")
    @GetMapping("/rooms/user")
    public ResponseEntity<List<ChatRoom>> userRooms(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return chatService.getUserRooms(userDetails.getUser());
    }
    @Operation(summary = "게시글 채팅방에 유저 추가(구독) -> 이거 쓰세요")
    @PostMapping("/room/post/{roomId}")
    @ResponseBody
    public ChatRoom addParticipantToChatRoomByPost(@PathVariable String roomId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return chatService.addParticipantToChatRoomByPost(roomId,userDetails.getUser());
    }
    @Operation(summary = "채팅방 나가기(구독 삭제) -> 이거 쓰세요")
    @DeleteMapping("/room/post/{roomId}")
    public ResponseEntity<String> leaveRoomByPost(@PathVariable String roomId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return chatService.leaveRoomByPost(roomId,userDetails.getUser());
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