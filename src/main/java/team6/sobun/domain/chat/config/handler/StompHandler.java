package team6.sobun.domain.chat.config.handler;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import team6.sobun.domain.chat.dto.ChatMessage;
import team6.sobun.domain.chat.repository.RedisChatRepository;
import team6.sobun.domain.chat.service.ChatService;
import team6.sobun.global.jwt.JwtProvider;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.security.SignatureException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    private final RedisChatRepository redisChatRepository;
    private final ChatService chatService;
    private final UserDetailsService userDetailsService;

    // websocket을 통해 들어온 요청이 처리 되기전 실행된다.
    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        log.info("어떤 preSend 메소드일까?: {}", accessor.getCommand());

        if (StompCommand.CONNECT == accessor.getCommand()) {
            String jwtToken = accessor.getFirstNativeHeader("Authorization");
            log.info("CONNECT {}", jwtToken);
            if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
                String token = jwtProvider.substringHeaderToken(jwtToken); // bearer 제거
                try {
                    jwtProvider.validateToken(token); // 유효성 검증
                    String nickname = jwtProvider.getNickNameFromToken(token); // 토큰에서 사용자 이름 추출

                    UserDetails userDetails = userDetailsService.loadUserByUsername(nickname);
                    if (userDetails != null) {
                        // 인증된 사용자이므로 필요한 작업을 수행합니다.
                        log.info("인증된 사용자입니다: {}", nickname);
                        // 여기서 userDetails를 활용하여 추가 작업을 수행할 수 있습니다.
                    } else {
                        log.error("인증되지 않은 사용자입니다: {}", nickname);
                        // 인증되지 않은 사용자 처리 로직
                        return null; // 인증되지 않은 사용자의 경우 연결을 막습니다.
                    }
                } catch (ExpiredJwtException e) {
                    log.error("Expired JWT token, 만료된 JWT token 입니다.");
                    // 만료된 토큰 처리 로직
                    return null; // 만료된 토큰의 경우 연결을 막습니다.
                } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
                    log.error("토큰 검증에 실패하였습니다: {}", e.getMessage());
                    // 토큰 검증 실패 처리 로직
                    return null; // 토큰 검증 실패 시 연결을 막습니다.
                }
            }

        } else if (StompCommand.SUBSCRIBE == accessor.getCommand()) {
            String jwtToken = accessor.getFirstNativeHeader("Authorization");
            if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
                String token = jwtProvider.substringHeaderToken(jwtToken); //
                try {
                    // header정보에서 구독 destination정보를 얻고, roomId를 추출한다.
                    String roomId = chatService.getRoomId(Optional.ofNullable((String) message.getHeaders().get("simpDestination")).orElse("InvalidRoomId"));
                    // 채팅방에 들어온 클라이언트 sessionId를 roomId와 맵핑해 놓는다.(나중에 특정 세션이 어떤 채팅방에 들어가 있는지 알기 위함)
                    String sessionId = (String) message.getHeaders().get("simpSessionId");
                    redisChatRepository.setUserEnterInfo(sessionId, roomId);
                    // 채팅방의 인원수를 +1한다.
                    redisChatRepository.plusUserCount(roomId);
                    // 클라이언트 입장 메시지를 채팅방에 발송한다.(redis publish)
                    String nickname = jwtProvider.getNickNameFromToken(token); // 토큰에서 사용자 이름 추출
                    chatService.sendChatMessage(ChatMessage.builder().type(ChatMessage.MessageType.ENTER).roomId(roomId).sender(nickname).build());
                    List<ChatMessage> chatMessages = redisChatRepository.getChatMessages(roomId);
                    chatMessages.forEach(chatService::sendChatMessage);
                    log.info("SUBSCRIBED {}, {}", nickname, roomId);
                } catch (Exception e) {
                    log.error("토큰 검증에 실패하였습니다: {}", e.getMessage());
                    // 토큰 검증 실패 처리 로직
                }
            }
        } else if (StompCommand.DISCONNECT == accessor.getCommand()) {
            String jwtToken = accessor.getFirstNativeHeader("Authorization");
            if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
                String token = jwtProvider.substringHeaderToken(jwtToken);
                try {
                    String nickname = jwtProvider.getNickNameFromToken(token); // 토큰에서 사용자 이름 추출
                    String sessionId = (String) message.getHeaders().get("simpSessionId");
                    String roomId = redisChatRepository.getUserEnterRoomId(sessionId);
                    // 채팅방의 인원수를 -1한다.
                    redisChatRepository.minusUserCount(roomId);
                    // 클라이언트 퇴장 메시지를 채팅방에 발송한다.(redis publish)
                    chatService.sendChatMessage(ChatMessage.builder().type(ChatMessage.MessageType.QUIT).roomId(roomId).sender(nickname).build());
                    redisChatRepository.removeUserEnterInfo(sessionId);
                    log.info("사용자가 채팅방을 나갔습니다 ! = {}", nickname);
                    log.info("DISCONNECTED {}, {}", sessionId, roomId);
                } catch (Exception e) {
                    log.error("토큰 검증에 실패하였습니다: {}", e.getMessage());
                    // 토큰 검증 실패 처리 로직
                }
            }

        } else if (StompCommand.SEND == accessor.getCommand()) {
            try {
                // header에서 roomId 추출
                String roomId = accessor.getFirstNativeHeader("roomId");
                log.info("roomId는 뭐야? roomId = {}", roomId);

                // 인증된 사용자의 이름 가져오기
                Authentication userAuthentication = SecurityContextHolder.getContext().getAuthentication();
                if (userAuthentication != null) {
                    String sender = userAuthentication.getName();
                    log.info("누가 보냈어? 보낸사람 = {}", sender);

                    byte[] payloadBytes = (byte[]) message.getPayload();
                    String messageContent = new String(payloadBytes, StandardCharsets.UTF_8);
                    log.info("내용은 뭐야? 메시지 내용 = {}", messageContent);

                    // 메시지 저장
                    ChatMessage chatMessage = ChatMessage.builder()
                            .type(ChatMessage.MessageType.TALK)
                            .roomId(roomId)
                            .sender(sender)
                            .message(messageContent)
                            .userCount(redisChatRepository.getUserCount(roomId))
                            .build();

                    redisChatRepository.storeChatMessage(chatMessage);
                    log.info("레디스에 채팅 메시지 저장됨: {} in room: {}", messageContent, roomId);

                } else {
                    log.error("인증되지 않은 사용자입니다.");
                }
            } catch (Exception e) {
                log.error("전송 중 에러 발생!! : {}", e.getMessage(), e);
            }
        }

        return message;
    }
}