package team6.sobun.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import team6.sobun.domain.chat.dto.ChatRoom;
import team6.sobun.domain.chat.repository.RedisChatRepository;
import team6.sobun.global.jwt.JwtProvider;
import team6.sobun.global.security.UserDetailsImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "채팅방 관련 API", description = "템플릿 연결 및 채팅방 생성 및 조회")
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/chat")
public class ChatRoomController {

    private final RedisChatRepository chatRoomRepository;
    private final JwtProvider jwtProvider;

    // chat/room 템플릿으로 진입
    @Operation(summary = "템플릿으로 진입")
    @GetMapping("/room")
    public String rooms(HttpServletRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String token = request.getHeader("Authorization"); // Authorization 헤더에서 토큰 가져오기
        if (token != null) {
        }
        return "chat/room";
    }
    // 전체 채팅방 조회
    @Operation(summary = "전체 채팅방 조회")
    @GetMapping("/rooms")
    @ResponseBody
    public List<ChatRoom> room() {
        List<ChatRoom> chatRoom = chatRoomRepository.findAllRoom();
        chatRoom.stream().forEach(room -> room.setUserCount(chatRoomRepository.getUserCount(room.getRoomId())));
        return chatRoom;
    }
    // 채팅방 생성
    @Operation(summary = "채팅방 생성")
    @PostMapping("/room")
    @ResponseBody
    public ChatRoom createRoom(@CookieValue("accessToken") String token) {
        String jwtToken = token.substring(7);
        String name = jwtProvider.getNickNameFromToken(jwtToken);
        ChatRoom chatRoom = chatRoomRepository.createChatRoom(name);
        return chatRoom;
    }
    // 채팅방 삭제
    @DeleteMapping("/room/{roomId}")
    public ResponseEntity<String> deleteRoom(@PathVariable String roomId, @CookieValue("accessToken") String token) {
        String jwtToken = token.substring(7);
        ChatRoom chatRoom = chatRoomRepository.findRoomById(roomId);
        // 채팅방이 존재하지 않는 경우
        if (chatRoom == null) {
            return ResponseEntity.notFound().build();
        }
        // 채팅방을 생성한 사용자와 현재 사용자가 같은지 확인
        if (!chatRoom.getName().equals(jwtProvider.getNickNameFromToken(jwtToken))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 할 권한이 없습니다.");
        }
        chatRoomRepository.deleteChatRoom(roomId);

        return ResponseEntity.ok("삭제 완료.");
    }
    // 특정 채팅방 조회
    @Operation(summary = "특정 채팅방 조회")
    @GetMapping("/room/{roomId}")
    @ResponseBody
    public ChatRoom roomInfo(@PathVariable String roomId) {
        return chatRoomRepository.findRoomById(roomId);
    }

    // 채팅 방 입장 페이지 ( 템플릿 진입 ) -> 스톰프 서버와 연결
    @Operation(summary = "채팅 방 입장 페이지(템플릿 진입)")
    @GetMapping("/room/enter/{roomId}")
    public String roomDetail(Model model, @PathVariable String roomId) {
        model.addAttribute("roomId", roomId);
        log.info("채팅 방 입장 페이지로 이동: roomId={}", roomId);
        return "chat/roomdetail";
    }
    // 프론트에서 SockJS로 스톰프 구현시 사용
    @Operation(summary = "SockJS")
    @GetMapping("/room/enterA/{roomId}")
    @ResponseBody
    public Map<String, String> roomDetail(@PathVariable String roomId) {
        Map<String, String> response = new HashMap<>();
        response.put("roomId", roomId);
        log.info("채팅 방 입장 페이지로 이동: roomId={}", roomId);
        return response;
    }
}