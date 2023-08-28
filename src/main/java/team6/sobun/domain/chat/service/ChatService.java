package team6.sobun.domain.chat.service;

import com.amazonaws.services.kms.model.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import team6.sobun.domain.chat.dto.ChatMessage;
import team6.sobun.domain.chat.dto.ChatMessageSearchCondition;
import team6.sobun.domain.chat.dto.ChatRoom;
import team6.sobun.domain.chat.entity.ChatMessageEntity;
import team6.sobun.domain.chat.entity.ChatRoomEntity;
import team6.sobun.domain.chat.repository.ChatMessageRepository;
import team6.sobun.domain.chat.repository.ChatRoomRepository;
import team6.sobun.domain.chat.repository.RedisChatRepository;
import team6.sobun.domain.post.service.S3Service;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class ChatService {

    private final RedisChatRepository redisChatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final RedisTemplate redisTemplate;
    private final ChannelTopic channelTopic;
    private final S3Service s3Service;
    


    public String getRoomId(String destination) {
        int lastIndex = destination.lastIndexOf('/');
        if (lastIndex != -1)
            return destination.substring(lastIndex + 1);
        else
            return "";
    }


    @Transactional
    public void sendChatMessage(ChatMessage chatMessage) {
        // 채팅방 내 인원 수 갱신
        chatMessage.setUserCount(redisChatRepository.getUserCount(chatMessage.getRoomId()));

        if (!ChatMessage.MessageType.ENTER.equals(chatMessage.getType()) &&
                !ChatMessage.MessageType.QUIT.equals(chatMessage.getType())) {
            // 일반 대화 메시지일 경우만 엔티티에 저장하고 발송
            ChatMessageEntity chatMessageEntity = new ChatMessageEntity(chatMessage);
            ChatRoomEntity chatRoomEntity = chatRoomRepository.findById(chatMessage.getRoomId())
                    .orElseThrow(() -> new NotFoundException("채팅방을 찾을 수 없습니다."));
            chatRoomEntity.updateLastMessage(chatMessage.getMessage(), chatMessage.getSender(), LocalDateTime.now());
            chatMessageRepository.save(chatMessageEntity);
        }
        if (ChatMessage.MessageType.ENTER.equals(chatMessage.getType())) {
            // 입장 메시지 처리
            chatMessage.setMessage(chatMessage.getSender() + "님이 방에 입장했습니다.");
            chatMessage.setSender("[알림]");
        } else if (ChatMessage.MessageType.QUIT.equals(chatMessage.getType())) {
            chatMessage.setMessage(chatMessage.getSender() + "님이 방에서 나갔습니다.");
            chatMessage.setSender("[알림]");
            // 퇴장 메시지 처리 및 마지막 메시지 ID 업데이트
        }

        // 메시지를 Redis Pub/Sub 채널로 발송
        redisTemplate.convertAndSend(channelTopic.getTopic(), chatMessage);
    }

    @Transactional(readOnly = true)
    public Slice<ChatMessage> getChatMessages(User user, String roomId, Pageable pageable) {
        ChatRoomEntity chatRoomEntity = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("채팅방을 찾을 수 없습니다."));
        LocalDateTime userEntryTime = chatRoomEntity.getEntryTimes().get(user.getNickname());
        ChatMessageSearchCondition condition = new ChatMessageSearchCondition(roomId, userEntryTime);
        condition.setRoomId(roomId);
        condition.setEntryTime(userEntryTime);
        return chatMessageRepository.searchChatMessages(condition, pageable);
    }

    public String uploadImage(MultipartFile file, User user) {
        String uploadedImageUrl = s3Service.upload(file);

        ChatMessage message = new ChatMessage();
        message.setType(ChatMessage.MessageType.IMAGE);
        message.setSender(user.getNickname());
        message.setRoomId(user.getRoomId());
        message.setImageUrl(uploadedImageUrl);

        // 여기서 message를 redis에 저장하고 발송하는 로직을 수행

        return uploadedImageUrl;
    }

///////////////////////////////////////////////////////////////////////////////////////////

    public ResponseEntity<List<ChatRoom>> getUserRooms(User user) {
        String nickname = user.getNickname();

        List<ChatRoomEntity> chatRoomEntities = chatRoomRepository.findByNicknames(nickname);

        List<ChatRoom> userRooms = chatRoomEntities.stream()
                .map(chatRoomEntity -> {
                    ChatRoom chatRoom = redisChatRepository.findRoomById(chatRoomEntity.getRoomId());
                    chatRoom.setUserCount(redisChatRepository.getUserCount(chatRoom.getRoomId()));
                    return chatRoom;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(userRooms);
    }
    public ChatRoomEntity createRoomByPost(String postTitle, User user) {
        ChatRoom chatRoom = redisChatRepository.createChatRoom(postTitle);

        ChatRoomEntity chatRoomEntity = new ChatRoomEntity();
        chatRoomEntity.setRoomId(chatRoom.getRoomId());
        chatRoomEntity.setTitle(postTitle);
        chatRoomEntity.setNicknames(Collections.singletonList(user.getNickname()));
        Map<String, LocalDateTime> entryTimes = new HashMap<>(chatRoomEntity.getEntryTimes());
        entryTimes.put(user.getNickname(), LocalDateTime.now()); // 최초 입장 시간 기록
        chatRoomEntity.setEntryTimes(entryTimes);
        chatRoomRepository.save(chatRoomEntity);
        return chatRoomEntity;
    }

    @Transactional
    public ChatRoom addParticipantToChatRoomByPost(String roomId, User user) {
        User newUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자 정보입니다."));
        String newUserNickname = newUser.getNickname();

        ChatRoom chatRoom = redisChatRepository.findRoomById(roomId);
        if (chatRoom == null) {
            throw new IllegalArgumentException("유효하지 않은 채팅방 정보입니다.");
        }

        ChatRoomEntity chatRoomEntity = chatRoomRepository.findById(roomId).orElse(null);
        if (chatRoomEntity != null) {
            List<String> nicknames = new ArrayList<>(chatRoomEntity.getNicknames());
            if (!nicknames.contains(newUserNickname)) {
                nicknames.add(newUserNickname);
                chatRoomEntity.setNicknames(nicknames);

                if (!chatRoomEntity.getEntryTimes().containsKey(newUserNickname)) {
                    Map<String, LocalDateTime> entryTimes = new HashMap<>(chatRoomEntity.getEntryTimes());
                    entryTimes.put(newUserNickname, LocalDateTime.now()); // 최초 입장 시간 기록
                    chatRoomEntity.setEntryTimes(entryTimes);
                }

                chatRoomRepository.save(chatRoomEntity);

                redisChatRepository.addUserToRoom(roomId, newUserNickname);
            }
        }
        return chatRoom;
    }
    @Transactional
    public ResponseEntity<String> leaveRoomByPost(String roomId, User user) {
        ChatRoom chatRoom = redisChatRepository.findRoomById(roomId);

        // 채팅방이 존재하지 않는 경우
        if (chatRoom == null) {
            return ResponseEntity.notFound().build();
        }

        String leavingUserNickname = user.getNickname();
        ChatRoomEntity chatRoomEntity = chatRoomRepository.findById(roomId).orElse(null);
        if (chatRoomEntity != null) {
            List<String> roomNicknames = chatRoomEntity.getNicknames();
            if (roomNicknames.size() > 1) {
                // 2명 이상의 사용자가 참여하는 채팅방의 경우
                boolean hasDeletePermission = roomNicknames.contains(leavingUserNickname);

                if (!hasDeletePermission) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 권한이 없습니다.");
                }
                // 나가는 사용자의 닉네임과 연관 관계를 삭제
                roomNicknames.remove(leavingUserNickname);
                chatRoomEntity.setNicknames(roomNicknames);

                Map<String, LocalDateTime> entryTimes = chatRoomEntity.getEntryTimes();
                entryTimes.remove(leavingUserNickname); // 유저의 입장 정보 삭제
                chatRoomEntity.setEntryTimes(entryTimes);

                chatRoomRepository.save(chatRoomEntity);
            } else {
                // 1명만 있는 채팅방인 경우
                if (!roomNicknames.get(0).equals(leavingUserNickname)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 권한이 없습니다.");
                }
                // 채팅방 삭제
                chatRoomRepository.deleteById(roomId);
            }
        }
        return ResponseEntity.ok("채팅방에서 나갔습니다.");
    }


    ////////////////////////////////////////////////////////////////////////////////

    public ChatRoom createRoom(String nickname) {
        ChatRoom chatRoom = redisChatRepository.createChatRoom(nickname);

        ChatRoomEntity chatRoomEntity = new ChatRoomEntity();
        chatRoomEntity.setRoomId(chatRoom.getRoomId());
        chatRoomEntity.setNicknames(Collections.singletonList(chatRoom.getName()));
        chatRoomEntity.setTitle(nickname);
        Map<String, LocalDateTime> entryTimes = new HashMap<>(chatRoomEntity.getEntryTimes());
        entryTimes.put(nickname, LocalDateTime.now()); // 최초 입장 시간 기록
        chatRoomEntity.setEntryTimes(entryTimes);
        chatRoomRepository.save(chatRoomEntity);

        return chatRoom;
    }
}