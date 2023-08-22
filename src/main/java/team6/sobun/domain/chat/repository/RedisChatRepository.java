package team6.sobun.domain.chat.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import team6.sobun.domain.chat.dto.ChatMessage;
import team6.sobun.domain.chat.dto.ChatRoom;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisChatRepository {

    // Redis CacheKeys
    private static final String CHAT_ROOMS = "CHAT_ROOM"; // 채팅룸 저장
    public static final String USER_COUNT = "USER_COUNT"; // 채팅룸에 입장한 클라이언트수 저장
    public static final String ENTER_INFO = "ENTER_INFO"; // 채팅룸에 입장한 클라이언트의 sessionId와 채팅룸 id를 맵핑한 정보 저장

    @Resource(name = "redisTemplate")
    private HashOperations<String, String, ChatRoom> hashOpsChatRoom;
    @Resource(name = "redisTemplate")
    private HashOperations<String, String, String> hashOpsEnterInfo;
    @Resource(name = "redisTemplate")
    private HashOperations<String, String, ChatMessage> hashOpsChatMessage;
    @Resource(name = "redisTemplate")
    private ValueOperations<String, String> valueOps;

    private final RedisTemplate<String, Object> redisTemplate;

    // 모든 채팅방 조회
    public List<ChatRoom> findAllRoom() {
        return hashOpsChatRoom.values(CHAT_ROOMS);
    }


    // 특정 채팅방 조회
    public ChatRoom findRoomById(String id) {
        return hashOpsChatRoom.get(CHAT_ROOMS, id);
    }

    // 대화 상대를 채팅방에 추가하는 메소드
    public void addUserToRoom(String roomId, String userNickname) {
        ChatRoom chatRoom = hashOpsChatRoom.get(CHAT_ROOMS, roomId);

        if (chatRoom != null) {
            chatRoom.getParticipants().add(userNickname);
            hashOpsChatRoom.put(CHAT_ROOMS, roomId, chatRoom);

            // 사용자가 참여한 채팅방 목록에 roomId를 추가
            hashOpsEnterInfo.put(ENTER_INFO + ":" + userNickname, roomId, roomId);
        } else {
            log.warn("채팅방이 존재하지 않습니다. roomId={}", roomId);
        }
    }

    public ChatRoom createChatRoom(String name) {
        ChatRoom chatRoom = ChatRoom.create(name);
        hashOpsChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId(), chatRoom);
        return chatRoom;
    }
    public void changeChatRoomName(String roomId, String newRoomName) {
        ChatRoom chatRoom = findRoomById(roomId);
        if (chatRoom != null) {
            chatRoom.setName(newRoomName);
            hashOpsChatRoom.put(CHAT_ROOMS, roomId, chatRoom);
        } else {
            log.warn("채팅방이 존재하지 않습니다. roomId={}", roomId);
        }
    }

    public void deleteChatRoom(String roomId) {
        // 해당 채팅방의 메시지를 해시에서 삭제
        String messagesKey = "CHAT_MESSAGES:" + roomId;
        redisTemplate.delete(messagesKey);

        // 해당 채팅방을 해시에서 삭제
        hashOpsChatRoom.delete(CHAT_ROOMS, roomId);

        // 해당 채팅방의 입장 정보를 삭제
        hashOpsEnterInfo.delete(ENTER_INFO, roomId);

        // 해당 채팅방의 유저 수 정보를 삭제
        valueOps.getOperations().delete(USER_COUNT + "_" + roomId);
    }

    public void saveMessage(String roomId, ChatMessage chatMessage) {
        String key = "CHAT_MESSAGES:" + roomId;
        long messageId = hashOpsChatMessage.size(key) + 1; // 이전 메시지 개수 + 1
        chatMessage.setMessageId(messageId); // 메시지에 고유한 messageId 추가
        hashOpsChatMessage.put(key, String.valueOf(messageId), chatMessage);
    }

    public List<ChatMessage> findMessagesByRoom(String roomId) {
        String key = "CHAT_MESSAGES:" + roomId;
        Map<String, ChatMessage> messages = hashOpsChatMessage.entries(key);
        List<ChatMessage> sortedMessages = new ArrayList<>(messages.values());

        sortedMessages.sort(Comparator.comparingLong(ChatMessage::getMessageId));

        return sortedMessages;
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

    public String findUserIdByVerificationToken(String verificationToken) {
        String key = "verificationToken:" + verificationToken;
        return (String) redisTemplate.opsForValue().get(key);
    }

    public void deleteVerificationToken(String verificationToken) {
        String key = "verificationToken:" + verificationToken;
        redisTemplate.delete(key);
    }

}