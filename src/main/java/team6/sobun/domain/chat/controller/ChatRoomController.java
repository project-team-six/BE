package team6.sobun.domain.chat.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import team6.sobun.domain.chat.dto.ChatRoom;
import team6.sobun.domain.chat.repository.RedisChatRepository;
import team6.sobun.global.jwt.JwtProvider;
import team6.sobun.global.security.UserDetailsImpl;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/chat")
public class ChatRoomController {

    private final RedisChatRepository chatRoomRepository;
    private final JwtProvider jwtProvider;


    @GetMapping("/room")
    public String rooms(HttpServletRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String token = request.getHeader("Authorization"); // Authorization 헤더에서 토큰 가져오기
        if (token != null) {
        }
        return "chat/room";
    }


    @GetMapping("/rooms")
    @ResponseBody
    public List<ChatRoom> room() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAllRoom();
        chatRooms.stream().forEach(room -> room.setUserCount(chatRoomRepository.getUserCount(room.getRoomId())));
        return chatRooms;
    }

    @PostMapping("/room")
    @ResponseBody
    public ChatRoom createRoom(@AuthenticationPrincipal UserDetailsImpl userDetails, HttpServletRequest request) {
        String token = jwtProvider.getTokenFromHeader(request);
        String accessToken = jwtProvider.substringHeaderToken(token);
        if (token != null && jwtProvider.validateToken(accessToken)) {
            String userId = userDetails.getUser().getNickname();
            ChatRoom chatRoom = chatRoomRepository.createChatRoom(userId);
            return chatRoom;
        } else {
            throw new RuntimeException("액세스 실패 = 유저정보 없음");
        }
    }


    @GetMapping("/room/enter/{roomId}")
    public String roomDetail(Model model, @PathVariable String roomId) {
        model.addAttribute("roomId", roomId);
        log.info("채팅 방 입장 페이지로 이동: roomId={}", roomId);
        return "chat/roomdetail";
    }

    @GetMapping("/room/{roomId}")
    @ResponseBody
    public ChatRoom roomInfo(@PathVariable String roomId) {
        return chatRoomRepository.findRoomById(roomId);
    }
}
