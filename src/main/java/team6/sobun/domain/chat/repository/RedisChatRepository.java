package team6.sobun.domain.chat.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import team6.sobun.domain.chat.dto.ChatMessage;
import team6.sobun.domain.chat.dto.ChatRoom;

import java.io.IOException;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisChatRepository {

    // Redis CacheKeys
    private static final String CHAT_ROOMS = "CHAT_ROOM"; // 채팅룸 저장
    private static final String CHAT_MESSAGES = "CHAT_MESSAGES";
    private static final String CHAT_MESSAGES_TEXT = "CHAT_MESSAGES_TEXT"; // 채팅 메시지를 문자열로 저장하는 키
    public static final String USER_COUNT = "USER_COUNT"; // 채팅룸에 입장한 클라이언트수 저장
    public static final String ENTER_INFO = "ENTER_INFO"; // 채팅룸에 입장한 클라이언트의 sessionId와 채팅룸 id를 맵핑한 정보 저장

    @Resource(name = "redisTemplate")
    private HashOperations<String, String, ChatRoom> hashOpsChatRoom;
    @Resource(name = "redisTemplate")
    private HashOperations<String, String, String> hashOpsEnterInfo;
    @Resource(name = "redisTemplate")
    private ValueOperations<String, String> valueOps;
    @Resource(name = "redisTemplate")
    private ListOperations<String, String> listOpsChatMessage;

    // 모든 채팅방 조회
    public List<ChatRoom> findAllRoom() {
        return hashOpsChatRoom.values(CHAT_ROOMS);
    }

    // 특정 채팅방 조회
    public ChatRoom findRoomById(String id) {
        return hashOpsChatRoom.get(CHAT_ROOMS, id);
    }

    public void storeChatMessage(ChatMessage chatMessage) {
        String roomId = chatMessage.getRoomId();
        if (roomId == null || roomId.isEmpty()) {
            log.error("유효하지 않은 채팅 메시지: roomId is null or empty");
            return;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String serializedMessage = objectMapper.writeValueAsString(chatMessage);

            if (chatMessage.getMessage() != null) {
                // 실제 메시지를 객체로 저장한 Redis 리스트에 추가
                listOpsChatMessage.leftPush(CHAT_MESSAGES + "_" + roomId, serializedMessage);
            } else {
                log.error("유효하지 않은 채팅 메시지: content={}", chatMessage.getMessage());
            }
        } catch (JsonProcessingException e) {
            log.error("채팅 메시지 저장 중 에러 발생: {}", e.getMessage(), e);
        }
    }


    public List<ChatMessage> getChatMessages(String roomId) {
        List<String> serializedMessages = listOpsChatMessage.range(CHAT_MESSAGES + "_" + roomId, 0, -1);
        List<ChatMessage> chatMessages = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        for (String serializedMessage : serializedMessages) {
            try {
                ChatMessage chatMessage = objectMapper.readValue(serializedMessage, ChatMessage.class);
                chatMessages.add(chatMessage);
            } catch (JsonProcessingException e) {
                log.error("채팅 조회 중 에러 발생: {}", e.getMessage(), e);
            }
        }

        log.info("채팅 메세지 뭐가 들어있어? = {}", chatMessages);
        return chatMessages;
    }

    // 채팅방 생성 : 서버간 채팅방 공유를 위해 redis hash에 저장한다.
    public ChatRoom createChatRoom(String name) {
        ChatRoom chatRoom = ChatRoom.create(name);
        hashOpsChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId(), chatRoom);
        return chatRoom;
    }

    // 유저가 입장한 채팅방ID와 유저 세션ID 맵핑 정보 저장
    public void setUserEnterInfo(String sessionId, String roomId) {
        hashOpsEnterInfo.put(ENTER_INFO, sessionId, roomId);
    }

    // 유저 세션으로 입장해 있는 채팅방 ID 조회
    public String getUserEnterRoomId(String sessionId) {
        return hashOpsEnterInfo.get(ENTER_INFO, sessionId);
    }

    // 유저 세션정보와 맵핑된 채팅방ID 삭제
    public void removeUserEnterInfo(String sessionId) {
        hashOpsEnterInfo.delete(ENTER_INFO, sessionId);
    }

    // 채팅방 유저수 조회
    public long getUserCount(String roomId) {
        return Long.valueOf(Optional.ofNullable(valueOps.get(USER_COUNT + "_" + roomId)).orElse("0"));
    }

    // 채팅방에 입장한 유저수 +1
    public long plusUserCount(String roomId) {
        return Optional.ofNullable(valueOps.increment(USER_COUNT + "_" + roomId)).orElse(0L);
    }

    // 채팅방에 입장한 유저수 -1
    public long minusUserCount(String roomId) {
        return Optional.ofNullable(valueOps.decrement(USER_COUNT + "_" + roomId)).filter(count -> count > 0).orElse(0L);
    }


}