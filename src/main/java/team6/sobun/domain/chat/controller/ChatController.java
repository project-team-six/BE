package team6.sobun.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import team6.sobun.domain.chat.dto.ChatMessage;
import team6.sobun.domain.chat.dto.ChatMessageSearchCondition;
import team6.sobun.domain.chat.repository.RedisChatRepository;
import team6.sobun.domain.chat.service.ChatService;
import team6.sobun.global.jwt.JwtProvider;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.security.UserDetailsImpl;

import java.util.List;

@Tag(name = "채팅 관련 API", description = "채팅방 안에서 메세지 전송 및 이미지 전송관련")
@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatController {
    private final JwtProvider jwtProvider;
    private final RedisChatRepository redisChatRepository;
    private final ChatService chatService;


    @Operation(summary = "메세지 보내기")
    @MessageMapping("/chat/message")
    public void message(ChatMessage message, @Header("Authorization") String token) {
        String nickname = jwtProvider.getNickNameFromToken(token.substring(9));
        String profileImageUrl = jwtProvider.getProfileImageUrlFromToken(token.substring(9));
        message.setSender(nickname);
        message.setProfileImageUrl(profileImageUrl);
        chatService.sendChatMessage(message);
    }

    @Operation(summary = "이전 메세지 목록 가져오기")
    @GetMapping("/chat/{roomId}")
    @ResponseBody
    public Slice<ChatMessage> getChatMessages(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                              @PathVariable String roomId,
                                              Pageable page) {
        return chatService.getChatMessages(userDetails.getUser(),roomId, page);
    }

    @Operation(summary = "채팅시 이미지 업로드")
    @PostMapping("/chat/image")
    @ResponseBody
    public ApiResponse<?> uploadImage(@RequestPart(value = "file", required = false) MultipartFile file,
                                      @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ApiResponse.success(chatService.uploadImage(file, userDetails.getUser()));
    }
}
