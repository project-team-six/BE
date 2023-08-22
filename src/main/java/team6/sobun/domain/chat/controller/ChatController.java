package team6.sobun.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import team6.sobun.domain.chat.dto.ChatMessage;
import team6.sobun.domain.chat.repository.RedisChatRepository;
import team6.sobun.domain.chat.service.ChatService;
import team6.sobun.domain.post.service.S3Service;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.repository.UserRepository;
import team6.sobun.global.jwt.JwtProvider;
import team6.sobun.global.responseDto.ApiResponse;

import java.util.List;
import java.util.Optional;

@Tag(name = "채팅 관련 API", description = "채팅방 안에서 메세지 전송 및 이미지 전송관련")
@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatController {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RedisChatRepository redisChatRepository;
    private final S3Service s3Service;
    private final ChatService chatService;


    @Operation(summary = "메세지 보내기")
    @MessageMapping("/chat/message")
    public void message(ChatMessage message, @Header("Authorization") String token)  {
        String nickname = jwtProvider.getNickNameFromToken(token.substring(9));
        message.setSender(nickname);
        message.setUserCount(redisChatRepository.getUserCount(message.getRoomId()));
        chatService.sendChatMessage(message);
        log.info("메시지 전송: sender={}, roomId={}, message={}", nickname, message.getRoomId(), message.getMessage());
    }
    @GetMapping("/chat/{roomId}")
    @ResponseBody
    public List<ChatMessage> getChatMessages(@CookieValue("accessToken") String token) {
        String jwtToken = token.substring(7);
        String email = jwtProvider.getEmailFromToken(jwtToken);

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new NullPointerException("유저를 찾을 수 없습니다."));

        List<ChatMessage> messages = redisChatRepository.findMessagesByRoom(user.getRoomId());

        return messages;
    }
    
    @Operation(summary = "채팅시 이미지 업로드")
    @PostMapping("/chat/image")
    @ResponseBody
    public ApiResponse<?> uploadImage(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @CookieValue("accessToken") String token) {
        log.info("토큰 어떻게 들어오냐..?={}",token);
        String jwtToken = token.substring(7);
        String email = jwtProvider.getEmailFromToken(jwtToken);
        log.info("이메일은? 어떻게 들어오냐..?={}",email);
        // 이미지를 S3에 업로드하고 업로드된 이미지 URL을 가져옴
        String uploadedImageUrl = s3Service.upload(file);
        // 토큰으로부터 유저 정보를 얻어오는 부분
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new NullPointerException("왜 안돼"));
        // 채팅 메시지로 이미지 URL을 보내기
        ChatMessage message = new ChatMessage();
        message.setType(ChatMessage.MessageType.IMAGE);
        message.setSender(user.getNickname());
        message.setRoomId(user.getRoomId());
        message.setImageUrl(uploadedImageUrl);
        log.info("무슨 메시지를? 보낸겨..?={}",message);
        return ApiResponse.success(uploadedImageUrl);
    }
}
