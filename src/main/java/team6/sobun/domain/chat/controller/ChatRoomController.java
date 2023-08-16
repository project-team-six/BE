package team6.sobun.domain.chat.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import team6.sobun.domain.chat.dto.ChatRoom;
import team6.sobun.domain.chat.entity.LoginInfo;
import team6.sobun.domain.chat.repository.RedisChatRepository;
import team6.sobun.domain.user.entity.UserRoleEnum;
import team6.sobun.global.jwt.JwtProvider;
import team6.sobun.global.security.UserDetailsImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/chat")
public class ChatRoomController {

    private final RedisChatRepository chatRoomRepository;
    private final JwtProvider jwtProvider;

    // chat/room 템플릿으로 진입
    @GetMapping("/room")
    public String rooms(HttpServletRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String token = request.getHeader("Authorization"); // Authorization 헤더에서 토큰 가져오기
        if (token != null) {
        }
        return "chat/room";
    }
    // 전체 채팅방 조회
    @GetMapping("/rooms")
    @ResponseBody
    public List<ChatRoom> room() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAllRoom();
        chatRooms.stream().forEach(room -> room.setUserCount(chatRoomRepository.getUserCount(room.getRoomId())));
        return chatRooms;
    }
    // 채팅방 생성
    @PostMapping("/room")
    @ResponseBody
    public ChatRoom createRoom(@RequestParam String name) {
        ChatRoom chatRoom = chatRoomRepository.createChatRoom(name);
        return chatRoom;
    }
    // 특정 채팅방 조회
    @GetMapping("/room/{roomId}")
    @ResponseBody
    public ChatRoom roomInfo(@PathVariable String roomId) {
        return chatRoomRepository.findRoomById(roomId);
    }

    // 채팅 방 입장 페이지 ( 템플릿 진입 ) -> 스톰프 서버와 연결
    @GetMapping("/room/enter/{roomId}")
    public String roomDetail(Model model, @PathVariable String roomId) {
        model.addAttribute("roomId", roomId);
        log.info("채팅 방 입장 페이지로 이동: roomId={}", roomId);
        return "chat/roomdetail";
    }
    // 프론트에서 SockJS로 스톰프 구현시 사용
    @GetMapping("/room/enterA/{roomId}")
    @ResponseBody
    public Map<String, String> roomDetail(@PathVariable String roomId) {
        Map<String, String> response = new HashMap<>();
        response.put("roomId", roomId);
        log.info("채팅 방 입장 페이지로 이동: roomId={}", roomId);
        return response;
    }
}