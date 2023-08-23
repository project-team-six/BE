package team6.sobun.domain.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import team6.sobun.domain.chat.dto.ChatMessage;
import team6.sobun.domain.chat.dto.ChatRoom;
import team6.sobun.domain.chat.entity.ChatRoomEntity;
import team6.sobun.domain.chat.repository.ChatRoomRepository;
import team6.sobun.domain.chat.repository.RedisChatRepository;
import team6.sobun.domain.post.service.S3Service;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.repository.UserRepository;
import team6.sobun.global.jwt.JwtProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class ChatService {

    private final RedisChatRepository redisChatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final RedisTemplate redisTemplate;
    private final ChannelTopic channelTopic;
    private final S3Service s3Service;
    private final JwtProvider jwtProvider;

    public String getRoomId(String destination) {
        int lastIndex = destination.lastIndexOf('/');
        if (lastIndex != -1)
            return destination.substring(lastIndex + 1);
        else
            return "";
    }

    public void sendChatMessage(ChatMessage chatMessage) {
        chatMessage.setUserCount(redisChatRepository.getUserCount(chatMessage.getRoomId()));
        if (!ChatMessage.MessageType.ENTER.equals(chatMessage.getType()) &&
                !ChatMessage.MessageType.QUIT.equals(chatMessage.getType())) {
            // 일반 대화 메시지일 경우만 저장하고 발송
            redisChatRepository.saveMessage(chatMessage.getRoomId(), chatMessage);
        }
        if (ChatMessage.MessageType.ENTER.equals(chatMessage.getType())) {
            chatMessage.setMessage(chatMessage.getSender() + "님이 방에 입장했습니다.");
            chatMessage.setSender("[알림]");
        } else if (ChatMessage.MessageType.QUIT.equals(chatMessage.getType())) {
            chatMessage.setMessage(chatMessage.getSender() + "님이 방에서 나갔습니다.");
            chatMessage.setSender("[알림]");
        }
        redisTemplate.convertAndSend(channelTopic.getTopic(), chatMessage);
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

    public ChatRoom createRoom(String nickname) {
        ChatRoom chatRoom = redisChatRepository.createChatRoom(nickname);

        ChatRoomEntity chatRoomEntity = new ChatRoomEntity();
        chatRoomEntity.setRoomId(chatRoom.getRoomId());
        chatRoomEntity.setNicknames(Collections.singletonList(chatRoom.getName()));
        chatRoomRepository.save(chatRoomEntity);

        return chatRoom;
    }

    public ChatRoomEntity createRoomByPost(String postTitle, User user) {
        ChatRoom chatRoom = redisChatRepository.createChatRoom(postTitle);

        ChatRoomEntity chatRoomEntity = new ChatRoomEntity();
        chatRoomEntity.setRoomId(chatRoom.getRoomId());
        chatRoomEntity.setPostTitle(postTitle);
        chatRoomEntity.setNicknames(Collections.singletonList(user.getNickname()));
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
        redisChatRepository.addUserToRoom(roomId, newUserNickname);

        ChatRoomEntity chatRoomEntity = chatRoomRepository.findById(roomId).orElse(null);
        if (chatRoomEntity != null) {
            List<String> nicknames = new ArrayList<>(chatRoomEntity.getNicknames());
            nicknames.add(newUserNickname);
                chatRoomEntity.setNicknames(nicknames);
                chatRoomRepository.save(chatRoomEntity);
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
                chatRoomRepository.save(chatRoomEntity);
            } else {
                // 1명만 있는 채팅방인 경우
                if (!roomNicknames.get(0).equals(leavingUserNickname)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 권한이 없습니다.");
                }
                // 채팅방 삭제
                redisChatRepository.deleteChatRoom(roomId);
                chatRoomRepository.deleteById(roomId); // 채팅방 엔티티 삭제
            }
        }
        return ResponseEntity.ok("채팅방에서 나갔습니다.");
    }


    ////////////////////////////////////////////////////////////////////////////////

    @Transactional
    public ChatRoom startChatRoom(User user, Long userId) {
        String currentUserNickname = user.getNickname();

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자 정보입니다."));
        String targetUserNickname = targetUser.getNickname();
        String roomName = currentUserNickname + ", " + targetUserNickname;
        ChatRoom chatRoom = redisChatRepository.createChatRoom(roomName);

        ChatRoomEntity chatRoomEntity = new ChatRoomEntity();
        chatRoomEntity.setRoomId(chatRoom.getRoomId());

        chatRoomEntity.addUser(currentUserNickname);
        chatRoomEntity.addUser(targetUserNickname);

        chatRoomRepository.save(chatRoomEntity);

        redisChatRepository.addUserToRoom(chatRoom.getRoomId(), currentUserNickname);

        return chatRoom;
    }

    @Transactional
    public ChatRoom addParticipantToChatRoom(String roomId, User user) {

        User targetUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자 정보입니다."));
        String targetUserNickname = targetUser.getNickname();

        ChatRoom chatRoom = redisChatRepository.findRoomById(roomId);
        if (chatRoom == null) {
            throw new IllegalArgumentException("유효하지 않은 채팅방 정보입니다.");
        }
        redisChatRepository.addUserToRoom(roomId, targetUserNickname);

        ChatRoomEntity chatRoomEntity = chatRoomRepository.findById(roomId).orElse(null);
        if (chatRoomEntity != null) {
            List<String> nicknames = new ArrayList<>(chatRoomEntity.getNicknames());
            nicknames.add(targetUserNickname);

            if (nicknames.size() > 2) {
                String newRoomName = String.join(", ", nicknames);
                chatRoomEntity.setNicknames(nicknames);
                redisChatRepository.changeChatRoomName(roomId, newRoomName);
                chatRoomRepository.save(chatRoomEntity);
            }
        }

        return chatRoom;
    }

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

    public ResponseEntity<String> deleteRoom(String roomId, User user) {
        ChatRoom chatRoom = redisChatRepository.findRoomById(roomId);

        // 채팅방이 존재하지 않는 경우
        if (chatRoom == null) {
            return ResponseEntity.notFound().build();
        }

        String[] userNicknames = chatRoom.getName().split(", ");
        String leavingUserNickname = user.getNickname();

        if (userNicknames.length > 1) {
            // 2명 이상의 사용자가 참여하는 채팅방의 경우
            boolean hasDeletePermission = Arrays.stream(userNicknames)
                    .anyMatch(nickname -> nickname.equals(leavingUserNickname));

            if (!hasDeletePermission) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 권한이 없습니다.");
            }

            ChatRoomEntity chatRoomEntity = chatRoomRepository.findById(roomId).orElse(null);
            if (chatRoomEntity != null) {
                // 나가는 사용자의 닉네임과 연관 관계를 삭제
                List<String> updatedNicknames = Arrays.stream(userNicknames)
                        .filter(nickname -> !nickname.equals(leavingUserNickname))
                        .collect(Collectors.toList());
                chatRoomEntity.setNicknames(updatedNicknames);

                // 변경된 닉네임으로 채팅방 이름 업데이트
                String newRoomName = String.join(", ", updatedNicknames);
                redisChatRepository.changeChatRoomName(roomId, newRoomName);

                chatRoomRepository.save(chatRoomEntity);
            }
        } else {
            // 1명만 있는 채팅방인 경우
            if (!userNicknames[0].equals(leavingUserNickname)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 권한이 없습니다.");
            }
            // 채팅방 삭제
            redisChatRepository.deleteChatRoom(roomId);
            chatRoomRepository.deleteById(roomId); // 채팅방 엔티티 삭제
        }
        return ResponseEntity.ok("채팅방에서 나갔습니다.");
    }
}