package team6.sobun.domain.chat.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import team6.sobun.domain.chat.dto.ChatRoom;
import team6.sobun.domain.chat.entity.LoginInfo;
import team6.sobun.domain.chat.repository.RedisChatRepository;
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
    public ChatRoom createRoom(@RequestParam String name) {
        ChatRoom chatRoom = chatRoomRepository.createChatRoom(name);
        return chatRoom;
    }

    @GetMapping("/room/enter/{roomId}")
    public String roomDetail(Model model, @PathVariable String roomId) {
        model.addAttribute("roomId", roomId);
        log.info("채팅 방 입장 페이지로 이동: roomId={}", roomId);
        return "chat/roomdetail";
    }
    @GetMapping("/room/enter2/{roomId}")
    @ResponseBody
    public Map<String, String> roomDetail(@PathVariable String roomId) {
        Map<String, String> response = new HashMap<>();
        response.put("roomId", roomId);
        log.info("채팅 방 입장 페이지로 이동: roomId={}", roomId);
        return response;
    }


    @GetMapping("/room/{roomId}")
    @ResponseBody
    public ChatRoom roomInfo(@PathVariable String roomId) {
        return chatRoomRepository.findRoomById(roomId);
    }

    @GetMapping("/user")
    @ResponseBody
    public LoginInfo getUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        log.info("이름이 뭐가 들어오지?={}",name);
        return LoginInfo.builder().name(name).token(jwtProvider.generateToken(name)).build();
    }
}