package team6.sobun.domain.chat.config.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import team6.sobun.domain.chat.dto.ChatMessage;
import team6.sobun.domain.chat.repository.RedisChatRepository;
import team6.sobun.domain.chat.service.ChatService;
import team6.sobun.global.jwt.JwtProvider;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    private final RedisChatRepository redisChatRepository;
    private final ChatService chatService;

    // websocket을 통해 들어온 요청이 처리 되기전 실행된다.
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        log.info("어떤 preSend 메소드일까?: {}", accessor.getCommand());
        if (StompCommand.CONNECT == accessor.getCommand()) {
            // 사용자 정보를 추출할 때 쿠키에서 액세스 토큰을 가져옴
            String jwtToken = accessor.getFirstNativeHeader("token");
            log.info("CONNECT {}", jwtToken);
            log.info("연결 완료!! 토큰은 1번 불러온다 !!");
            // Header의 jwt token 검증
            jwtProvider.validateToken(jwtToken);
        } else if (StompCommand.SUBSCRIBE == accessor.getCommand()) { // 채팅룸 구독요청
            // header정보에서 구독 destination정보를 얻고, roomId를 추출한다.
            String roomId = chatService.getRoomId(Optional.ofNullable((String) message.getHeaders().get("simpDestination")).orElse("InvalidRoomId"));
            // 채팅방에 들어온 클라이언트 sessionId를 roomId와 맵핑해 놓는다.(나중에 특정 세션이 어떤 채팅방에 들어가 있는지 알기 위함)
            String sessionId = (String) message.getHeaders().get("simpSessionId");
            redisChatRepository.setUserEnterInfo(sessionId, roomId);
            // 채팅방의 인원수를 +1한다.
            redisChatRepository.plusUserCount(roomId);
            // 클라이언트 입장 메시지를 채팅방에 발송한다.(redis publish)
            String name = Optional.ofNullable((Principal) message.getHeaders().get("simpUser")).map(Principal::getName).orElse("UnknownUser");
            chatService.sendChatMessage(ChatMessage.builder().type(ChatMessage.MessageType.ENTER).roomId(roomId).sender(name).build());
            // 이전 채팅 내용 가져오기
            List<ChatMessage> chatMessages = redisChatRepository.getChatMessages(roomId);
            log.info("채팅 메세지 뭐가 들어있어? ={}", chatMessages);
            chatMessages.forEach(chatMessage -> {
                // 이전 채팅 메시지들을 클라이언트에게 전송
                chatService.sendChatMessage(chatMessage);
            });
            log.info("SUBSCRIBED {}, {}", name, roomId);

        } else if (StompCommand.DISCONNECT == accessor.getCommand()) { // Websocket 연결 종료
            // 연결이 종료된 클라이언트 sesssionId로 채팅방 id를 얻는다.
            String sessionId = (String) message.getHeaders().get("simpSessionId");
            String roomId = redisChatRepository.getUserEnterRoomId(sessionId);
            // 채팅방의 인원수를 -1한다.
            redisChatRepository.minusUserCount(roomId);
            // 클라이언트 퇴장 메시지를 채팅방에 발송한다.(redis publish)
            String name = Optional.ofNullable((Principal) message.getHeaders().get("simpUser")).map(Principal::getName).orElse("UnknownUser");
            chatService.sendChatMessage(ChatMessage.builder().type(ChatMessage.MessageType.QUIT).roomId(roomId).sender(name).build());
            // 퇴장한 클라이언트의 roomId 맵핑 정보를 삭제한다.
            redisChatRepository.removeUserEnterInfo(sessionId);
            log.info("사용자가 채팅방을 나갔습니다 ! = {}", name);
            log.info("DISCONNECTED {}, {}", sessionId, roomId);
        } else if (StompCommand.SEND == accessor.getCommand()) {
            try {
                String roomId = chatService.getRoomId(accessor.getDestination());
                log.info("들어오나? = {}", roomId);
                String sender = Optional.ofNullable((Principal) message.getHeaders().get("simpUser")).map(Principal::getName).orElse("UnknownUser");
                log.info("들어오나? = {}", sender);
                byte[] payloadBytes = (byte[]) message.getPayload();
                String messageContent = new String(payloadBytes, StandardCharsets.UTF_8);
                log.info("들어오나? = {}", messageContent);

                // Message 저장
                ChatMessage chatMessage = ChatMessage.builder()
                        .type(ChatMessage.MessageType.TALK)
                        .roomId(roomId)
                        .sender(sender)
                        .message(messageContent)
                        .userCount(redisChatRepository.getUserCount(roomId))
                        .build();

                redisChatRepository.storeChatMessage(chatMessage);
                log.info("레디스에 채팅 메세지 저장되었니? : {} in room: {}", messageContent, roomId);

            } catch (Exception e) {
                log.error("SEND 일때 에러 이녀석 잡았다!! : {}", e.getMessage(), e);
            }
        }   return message;
    }
}