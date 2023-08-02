
package team6.sobun.domain.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import team6.sobun.domain.chat.dto.ChatMessage;
import team6.sobun.domain.chat.dto.ChatRoom;
import team6.sobun.domain.chat.entity.ChatMessageEntity;
import team6.sobun.domain.chat.entity.ChatRoomEntity;
import team6.sobun.domain.chat.repository.ChatMessageRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {
    private final ObjectMapper objectMapper;
    private Map<String, ChatRoom> chatRooms;
    private final ChatMessageRepository chatMessageRepository;
    private final RedisMessageListenerContainer redisMessageListenerContainer;

    private final ChannelTopic channelTopic = new ChannelTopic("chat-room");

    @PostConstruct
    private void init() {
        chatRooms = new LinkedHashMap<>();
        redisMessageListenerContainer.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message, byte[] pattern) {
                String payload = new String(message.getBody());
                try {
                    ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);
                    ChatRoom chatRoom = findRoomById(chatMessage.getRoomId());
                    if (chatRoom != null) {
                        handleActions(null, chatMessage, chatMessage.getSender());
                    }
                } catch (IOException e) {
                    log.error("Error handling Redis message", e);
                }
            }
        }, channelTopic);
    }

    public List<ChatRoom> findAllRoom() {
        return new ArrayList<>(chatRooms.values());
    }

    public ChatRoom findRoomById(String roomId) {
        return chatRooms.get(roomId);
    }

    public ChatRoom createRoom(String name, String username) {
        String randomId = UUID.randomUUID().toString();
        ChatRoom chatRoom = ChatRoom.builder()
                .roomId(randomId)
                .name(name)
                .build();
        chatRooms.put(randomId, chatRoom);
        chatRoom.addParticipant(username); // 채팅방 생성 시, 사용자를 참가자로 추가합니다.
        return chatRoom;
    }

    public void handleActions(WebSocketSession session, ChatMessage chatMessage, String username) {
        ChatRoom chatRoom = findRoomById(chatMessage.getRoomId());

        if (chatMessage.getType().equals(ChatMessage.MessageType.ENTER)) {
            // 이 부분은 Redis Pub/Sub으로 이동하였으므로 삭제합니다.
        } else if (chatMessage.getType().equals(ChatMessage.MessageType.TALK)) {
            // 메시지를 채팅방에 전송합니다.
            sendMessageToChatRoom(chatMessage, chatRoom);

            // DB에 채팅 메시지를 저장합니다.
            saveChatMessageToDB(chatMessage, chatRoom);
        }
    }

    public <T> void sendMessage(WebSocketSession session, T message) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void sendMessageToChatRoom(ChatMessage chatMessage, ChatRoom chatRoom) {
        for (WebSocketSession session : chatRoom.getSessions()) {
            sendMessage(session, chatMessage);
        }
    }

    private void saveChatMessageToDB(ChatMessage chatMessage, ChatRoom chatRoom) {
        chatMessageRepository.save(ChatMessageEntity.builder()
                .chatRoom(ChatRoomEntity.builder().roomId(chatRoom.getRoomId()).build())
                .type(chatMessage.getType().name())
                .sender(chatMessage.getSender())
                .message(chatMessage.getMessage())
                .createdAt(LocalDateTime.now())
                .build());
    }
}
