package team6.sobun.domain.chat.config.handler;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import team6.sobun.domain.chat.dto.ChatRoom;
import team6.sobun.domain.chat.entity.ChatRoomEntity;
import team6.sobun.domain.chat.entity.ChatRoomParticipant;
import team6.sobun.domain.chat.repository.ChatRoomRepository;
import team6.sobun.domain.chat.repository.RedisChatRepository;
import team6.sobun.domain.chat.service.ChatService;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.repository.UserRepository;
import team6.sobun.global.jwt.JwtProvider;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RedisChatRepository redisChatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatService chatService;
    private final UserDetailsService userDetailsService;

    // websocket을 통해 들어온 요청이 처리 되기전 실행된다.
    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String token = accessor.getFirstNativeHeader("Authorization");
        String jwtToken = token != null ? token.substring(9) : null;
        log.info("어떤 preSend 메소드일까?: {}", accessor.getCommand());


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
        // header 정보에서 구독 destination 정보를 얻고, roomId를 추출한다.
        String roomId = chatService.getRoomId(
                Optional.ofNullable((String) message.getHeaders().get("simpDestination")).orElse("InvalidRoomId")
        );

        // 채팅방에 들어온 클라이언트 sessionId를 roomId와 맵핑해 놓는다.
        String sessionId = (String) message.getHeaders().get("simpSessionId");

        // 채팅방 엔터티 조회
            ChatRoom chatRoom = redisChatRepository.findRoomById(roomId);
        ChatRoomEntity chatRoomEntity = chatRoomRepository.findById(chatRoom.getRoomId()).orElse(null);

        if (chatRoomEntity == null) {
            log.error("채팅방을 찾을 수 없습니다 = {}", roomId);
            return null;
        }
        Long userId = Long.valueOf(jwtProvider.getUserIdFromToken(jwtToken));
        // 채팅방의 참가자 목록에서 세션 ID와 일치하는 참가자 정보 가져오기
        ChatRoomParticipant participant = chatRoomEntity.getChatRoomparticipants().stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst()
                .orElse(null);

            if (participant == null) {
                log.error("존재하지 않은 참가자 입니다 = {}", roomId);
                // 존재하지 않는 참가자라면 연결을 끊음
                forceDisconnectUser(sessionId);
                return null;
            }

        redisChatRepository.setUserEnterInfo(sessionId, roomId);

        // 채팅방의 인원수를 +1한다.
        redisChatRepository.plusUserCount(roomId);
        String email = jwtProvider.getEmailFromToken(jwtToken); // jwtProvider를 이용해 토큰에서 사용자 이름 추출
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new NullPointerException("왜 안돼"));
        user.setSessionId(sessionId);
        user.setRoomId(roomId);
        userRepository.save(user);
        log.info("SUBSCRIBED 메소드 :SUBSCRIBED {}, {}", sessionId, roomId);
    }else if (StompCommand.DISCONNECT == accessor.getCommand()) {
            // 연결이 종료된 클라이언트 sesssionId로 채팅방 id를 얻는다.
            String sessionId = (String) message.getHeaders().get("simpSessionId");
            String roomId = redisChatRepository.getUserEnterRoomId(sessionId);
            // 채팅방의 인원수를 -1한다.
            redisChatRepository.minusUserCount(roomId);
            User user = userRepository.findBySessionId(sessionId);
            user.setSessionId(null);
            user.setRoomId(null);

            userRepository.save(user);
            redisChatRepository.removeUserEnterInfo(sessionId);
            log.info("DISCONNECT 메소드 : DISCONNECTED  {}, {}", sessionId, roomId);

        }

        return message;
    }

    @Transactional
    public ResponseEntity<String> disconnectUser(User user, Long targetId, String roomId) {
        ChatRoomEntity chatRoomEntity = chatRoomRepository.findById(roomId).orElse(null);

        if (chatRoomEntity == null) {
            return ResponseEntity.notFound().build();
        }

        User targetUser = userRepository.findById(targetId).orElse(null);
        if (targetUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("찾을 수 없는 사용자입니다.");
        }

        // 현재 유저가 채팅방의 방장인지 확인
        if (chatRoomEntity.getMasterId().equals(user.getId())) {
            ChatRoomParticipant targetParticipant = chatRoomEntity.getChatRoomparticipants().stream()
                    .filter(participant -> participant.getUserId().equals(targetUser.getId()))
                    .findFirst()
                    .orElse(null);

            if (targetParticipant == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("찾을 수 없는 참가자입니다.");
            }

            String targetSessionId = targetUser.getSessionId();
            forceDisconnectUser(targetSessionId);

            // 채팅방에서 해당 참가자 제거
            chatRoomEntity.getChatRoomparticipants().remove(targetParticipant);
            chatRoomRepository.save(chatRoomEntity);

            return ResponseEntity.ok(targetUser.getNickname() + "님을 강제로 강퇴했습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("방장만 강퇴할 수 있습니다.");
        }
    }

    public void forceDisconnectUser(String sessionId) {
        log.info("강제 강퇴 메소드 호출: 세션 ID = {}", sessionId);
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        accessor.setSessionId(sessionId);
        accessor.setLeaveMutable(true);
        Message<byte[]> disconnectMessage = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        SimpMessagingTemplate messagingTemplate = ApplicationContextProvider.getBean(SimpMessagingTemplate.class);
        messagingTemplate.send("/topic/disconnect", disconnectMessage);
        }
    }


