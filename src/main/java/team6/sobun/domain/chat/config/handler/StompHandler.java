package team6.sobun.domain.chat.config.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import team6.sobun.domain.chat.dto.ChatMessage;
import team6.sobun.domain.chat.repository.RedisChatRepository;
import team6.sobun.domain.chat.service.ChatService;
import team6.sobun.domain.post.service.S3Service;
import team6.sobun.global.jwt.JwtProvider;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    private final RedisChatRepository redisChatRepository;
    private final ChatService chatService;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;
    private final S3Service s3Service;

    // websocket을 통해 들어온 요청이 처리 되기전 실행된다.
    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String jwtToken = accessor.getFirstNativeHeader("Authorization");
        log.info(" 어떤 preSend 메소드일까?: {}", accessor.getCommand());

        if (StompCommand.CONNECT == accessor.getCommand()) {
            log.info("CONNECT 메소드 : CONNECT {}", jwtToken);
            if (jwtToken != null) {
                try {
                    jwtProvider.validateToken(jwtToken); // 유효성 검증
                    String nickname = jwtProvider.getNickNameFromToken(jwtToken); // 토큰에서 사용자 이름 추출

                    UserDetails userDetails = userDetailsService.loadUserByUsername(nickname);
                    if (userDetails != null) {
                        // 인증된 사용자이므로 필요한 작업을 수행합니다.
                        log.info("CONNECT 메소드 : 인증된 사용자입니다: {}", nickname);
                        // 여기서 userDetails를 활용하여 추가 작업을 수행할 수 있습니다.
                    } else {
                        log.error("CONNECT 메소드 : 인증되지 않은 사용자입니다: {}", nickname);
                        // 인증되지 않은 사용자 처리 로직
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
        } else if (StompCommand.SUBSCRIBE == accessor.getCommand()) { // 채팅룸 구독요청
            // header정보에서 구독 destination정보를 얻고, roomId를 추출한다.
            String roomId = chatService.getRoomId(Optional.ofNullable((String) message.getHeaders().get("simpDestination")).orElse("InvalidRoomId"));
            // 채팅방에 들어온 클라이언트 sessionId를 roomId와 맵핑해 놓는다.(나중에 특정 세션이 어떤 채팅방에 들어가 있는지 알기 위함)
            String sessionId = (String) message.getHeaders().get("simpSessionId");
            redisChatRepository.setUserEnterInfo(sessionId, roomId);
            // 채팅방의 인원수를 +1한다.
            redisChatRepository.plusUserCount(roomId);
            // 인증된 사용자의 이름 가져오기
            String name = jwtProvider.getNickNameFromToken(jwtToken); // jwtProvider를 이용해 토큰에서 사용자 이름 추출
            log.info("SUBSCRIBED 메소드 :인증된 사용자입니다: {}", name);
            // 클라이언트 입장 메시지를 채팅방에 발송한다.(redis publish)
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

                // 퇴장한 클라이언트의 roomId 맵핑 정보를 삭제한다.
                redisChatRepository.removeUserEnterInfo(sessionId);
                log.info("DISCONNECT 메소드 : DISCONNECTED {}, {}, {}", name, sessionId, roomId);
            }
        } else if (StompCommand.SEND == accessor.getCommand()) {
            try {
                // header에서 roomId 추출
                String sessionId = (String) message.getHeaders().get("simpSessionId");
                String roomId = redisChatRepository.getUserEnterRoomId(sessionId);
                log.info("SEND 메소드 : roomId는 뭐야? roomId = {}", roomId);
                // 인증된 사용자의 이름 가져오기
                String sender = jwtProvider.getNickNameFromToken(jwtToken);
                log.info("SEND 메소드 : sender는 뭐야?={}", sender);

                byte[] payloadBytes = (byte[]) message.getPayload();
                String messageContent = new String(payloadBytes, StandardCharsets.UTF_8);
                log.info("SEND 메소드 : 내용은 뭐야? 메시지 내용 = {}", messageContent);

                ChatMessage originalChatMessage = objectMapper.readValue(messageContent, ChatMessage.class);
                ChatMessage.MessageType messageType = originalChatMessage.getType();

                if (messageType == ChatMessage.MessageType.IMAGE) {
                    // 이미지 메시지 처리 로직 추가
                    String imageUrl = originalChatMessage.getImageUrl();
                    log.info("들어오나 이미지={}",imageUrl);
                    // 이미지를 S3에 업로드하고 URL을 반환
                    MultipartFile imageFile = getMultipartFileFromUrl(imageUrl);
                    log.info("들어오나 이미지파일={}",imageFile);
                    if (imageFile != null) {
                        String uploadedImageUrl = s3Service.upload(imageFile);
                        log.info("들어오나 업로드 이미지={}",uploadedImageUrl);
                        // ChatMessage의 imageUrl 설정
                        ChatMessage chatMessage = ChatMessage.builder()
                                .type(ChatMessage.MessageType.IMAGE)
                                .roomId(roomId)
                                .sender(sender)
                                .imageUrl(uploadedImageUrl)
                                .userCount(redisChatRepository.getUserCount(roomId))
                                .build();

                        redisChatRepository.storeChatMessage(chatMessage);
                        log.info("레디스에 이미지 메시지 저장됨: {} in room: {}", imageUrl, roomId);
                    }
                } else if (messageType == ChatMessage.MessageType.TALK) {
                    ChatMessage chatMessage = ChatMessage.builder()
                            .type(ChatMessage.MessageType.TALK)
                            .roomId(roomId)
                            .sender(sender)
                            .message(messageContent)
                            .imageUrl(originalChatMessage.getImageUrl())
                            .userCount(redisChatRepository.getUserCount(roomId))
                            .build();

                    redisChatRepository.storeChatMessage(chatMessage);
                    log.info("레디스에 채팅 메시지 저장됨: {} in room: {}", messageContent, roomId);
                }
            } catch (JsonMappingException e) {
                throw new RuntimeException(e);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return message;
    }

    // 이미지 URL을 기반으로 MultipartFile을 생성하는 메서드
    private MultipartFile getMultipartFileFromUrl(String imageUrl) {
        try {
            if (StringUtils.hasText(imageUrl)) {
                URL url = new URL(imageUrl);
                // 이미지를 S3에 업로드하지 않고, URL을 그대로 사용
                String imageName = generateFileNameFromUrl(imageUrl);
                String contentType = getContentTypeFromUrl(imageUrl);

                // 이미지 데이터를 가져오기 위한 InputStream
                try (InputStream inputStream = url.openStream()) {
                    // MockMultipartFile로 생성
                    return new MockMultipartFile(
                            imageName, imageName, contentType, inputStream
                    );
                }
            } else {
                log.error("이미지 URL이 유효하지 않습니다.");
                return null;
            }
        } catch (Exception e) {
            log.error("이미지 가져오기 실패: {}", e.getMessage(), e);
            return null;
        }
    }


    // 이미지 URL에서 파일 이름 추출
    private String generateFileNameFromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            String path = url.getPath();
            String[] pathSegments = path.split("/");
            return pathSegments[pathSegments.length - 1];
        } catch (Exception e) {
            log.error("파일 이름 추출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("파일 이름 추출 실패", e);
        }
    }

    // 이미지 URL에서 컨텐츠 타입 추출
    private String getContentTypeFromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            return connection.getContentType();
        } catch (Exception e) {
            log.error("컨텐츠 타입 추출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("컨텐츠 타입 추출 실패", e);
        }
    }
}

