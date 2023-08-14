package team6.sobun.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import team6.sobun.domain.chat.dto.ChatMessage;
import team6.sobun.domain.chat.repository.RedisChatRepository;
import team6.sobun.domain.chat.service.ChatService;
import team6.sobun.global.jwt.JwtProvider;
import team6.sobun.global.security.UserDetailsImpl;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatController {

    private final JwtProvider jwtProvider;
    private final RedisChatRepository redisChatRepository;
    private final ChatService chatService;

    /**
     * websocket "/pub/chat/message"로 들어오는 메시징을 처리한다.
     */
    @MessageMapping("/chat/message")
    public void message(ChatMessage message, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails != null) {
            try {
                String nickname = userDetails.getNickname();
                // 로그인 회원 정보로 대화명 설정
                message.setSender(nickname);
                log.info("닉네임은 뭐야?={}", nickname);
                // 채팅방 인원수 세팅
                message.setUserCount(redisChatRepository.getUserCount(message.getRoomId()));
                // Websocket에 발행된 메시지를 redis로 발행(publish)
                chatService.sendChatMessage(message);

                log.info("메시지 전송: sender={}, roomId={}, message={}", nickname, message.getRoomId(), message.getMessage());
            } catch (NullPointerException e) {
                log.error("토큰이 유효하지 않습니다.", e);
            }
        } else {
            log.error("jwtToken 헤더가 누락되었습니다.");
        }
    }
}