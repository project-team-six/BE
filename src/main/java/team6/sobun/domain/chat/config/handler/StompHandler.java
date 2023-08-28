package team6.sobun.domain.chat.config.handler;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import team6.sobun.domain.chat.dto.ChatMessage;
import team6.sobun.domain.chat.entity.ChatMessageEntity;
import team6.sobun.domain.chat.entity.ChatRoomEntity;
import team6.sobun.domain.chat.repository.ChatMessageRepository;
import team6.sobun.domain.chat.repository.ChatRoomRepository;
import team6.sobun.domain.chat.repository.RedisChatRepository;
import team6.sobun.domain.chat.service.ChatService;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.repository.UserRepository;
import team6.sobun.global.jwt.JwtProvider;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RedisChatRepository redisChatRepository;
    private final ChatService chatService;
    private final UserDetailsService userDetailsService;

    // websocket을 통해 들어온 요청이 처리 되기전 실행된다.
    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String token = accessor.getFirstNativeHeader("Authorization");
        String jwtToken = token != null ? token.substring(9) : null;
        log.info(" 어떤 preSend 메소드일까?: {}", accessor.getCommand());
        if (StompCommand.CONNECT == accessor.getCommand()) {
            if (StringUtils.hasText(jwtToken)) {
                log.info("CONNECT 메소드 : CONNECT {}", jwtToken);
                try {
                    jwtProvider.validateToken(jwtToken); // 유효성 검증
                    String email = jwtProvider.getEmailFromToken(jwtToken); // 토큰에서 사용자 이름 추출
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    if (userDetails != null) {
                        // 인증된 사용자이므로 필요한 작업을 수행합니다.
                        log.info("CONNECT 메소드 : 인증된 사용자입니다: {}", email);
                    } else {
                        log.error("CONNECT 메소드 : 인증되지 않은 사용자입니다: {}", email);
                        return null; // 인증되지 않은 사용자의 경우 연결을 막습니다.
                    }
                } catch (ExpiredJwtException e) {
                    log.error("CONNECT 메소드 : Expired JWT token, 만료된 JWT token 입니다.");
                    // 만료된 토큰 처리 로직
                    return null; // 만료된 토큰의 경우 연결을 막습니다.
                } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
                    log.error("CONNECT 메소드 : 토큰 검증에 실패하였습니다: {}", e.getMessage());
                    // 토큰 검증 실패 처리 로직
                    return null; // 토큰 검증 실패 시 연결을 막습니다.
                }
            }
        } else if (StompCommand.SUBSCRIBE == accessor.getCommand()) {// 채팅룸 구독요청

            // header정보에서 구독 destination정보를 얻고, roomId를 추출한다.
            String roomId = chatService.getRoomId(Optional.ofNullable((String) message.getHeaders().get("simpDestination")).orElse("InvalidRoomId"));
            // 채팅방에 들어온 클라이언트 sessionId를 roomId와 맵핑해 놓는다.(나중에 특정 세션이 어떤 채팅방에 들어가 있는지 알기 위함)
            String sessionId = (String) message.getHeaders().get("simpSessionId");
            redisChatRepository.setUserEnterInfo(sessionId, roomId);
            // 채팅방의 인원수를 +1한다.
            redisChatRepository.plusUserCount(roomId);
            // 인증된 사용자의 이름 가져오기
            String email = jwtProvider.getEmailFromToken(jwtToken); // jwtProvider를 이용해 토큰에서 사용자 이름 추출
            String name = jwtProvider.getNickNameFromToken(jwtToken);
            log.info("SUBSCRIBED 메소드 :인증된 사용자입니다: {}", name);
            User user = userRepository.findByEmail(email).orElseThrow(
                    () -> new NullPointerException("왜 안돼"));
            user.setSessionId(sessionId);
            user.setRoomId(roomId);
            userRepository.save(user);
            chatService.sendChatMessage(ChatMessage.builder().type(ChatMessage.MessageType.ENTER).roomId(roomId).sender(name).build());
            log.info("SUBSCRIBED 메소드 :SUBSCRIBED {}, {}", name, roomId);

        } else if (StompCommand.DISCONNECT == accessor.getCommand()) {
            // 연결이 종료된 클라이언트 sesssionId로 채팅방 id를 얻는다.
            String sessionId = (String) message.getHeaders().get("simpSessionId");
            String roomId = redisChatRepository.getUserEnterRoomId(sessionId);
            // 채팅방의 인원수를 -1한다.
            redisChatRepository.minusUserCount(roomId);
            if (jwtToken != null) {
                String name = jwtProvider.getNickNameFromToken(jwtToken);
                // 클라이언트 퇴장 메시지를 채팅방에 발송한다.(redis publish)
                chatService.sendChatMessage(ChatMessage.builder().type(ChatMessage.MessageType.QUIT).roomId(roomId).sender(name).build());
                User user = userRepository.findBySessionId(sessionId);
                user.setSessionId(null);
                user.setRoomId(null);
                userRepository.save(user);
                redisChatRepository.removeUserEnterInfo(sessionId);
                log.info("DISCONNECT 메소드 : DISCONNECTED {}, {}, {}", name, sessionId, roomId);

            }
        }

        return message;
    }
}

