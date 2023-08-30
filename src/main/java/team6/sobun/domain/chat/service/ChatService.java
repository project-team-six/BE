package team6.sobun.domain.chat.service;

import com.amazonaws.services.kms.model.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import team6.sobun.domain.chat.dto.ChatMessage;
import team6.sobun.domain.chat.dto.ChatMessageSearchCondition;
import team6.sobun.domain.chat.dto.ChatReportRequestDto;
import team6.sobun.domain.chat.dto.ChatRoom;
import team6.sobun.domain.chat.entity.ChatMessageEntity;
import team6.sobun.domain.chat.entity.ChatReportEntity;
import team6.sobun.domain.chat.entity.ChatRoomEntity;
import team6.sobun.domain.chat.entity.ChatRoomParticipant;
import team6.sobun.domain.chat.repository.ChatMessageRepository;
import team6.sobun.domain.chat.repository.ChatReportRepository;
import team6.sobun.domain.chat.repository.ChatRoomRepository;
import team6.sobun.domain.chat.repository.RedisChatRepository;
import team6.sobun.domain.post.dto.PostRequestDto;
import team6.sobun.domain.post.service.S3Service;
import team6.sobun.domain.user.entity.User;
import team6.sobun.domain.user.repository.UserRepository;
import team6.sobun.global.jwt.JwtProvider;
import team6.sobun.global.responseDto.ApiResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static team6.sobun.global.stringCode.SuccessCodeEnum.CHAT_REPORT_SUCCESS;
import static team6.sobun.global.stringCode.SuccessCodeEnum.POST_REPORT_SUCCESS;

@RequiredArgsConstructor
@Service
@Slf4j
public class ChatService {

    private final RedisChatRepository redisChatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatReportRepository chatReportRepository;
    private final UserRepository userRepository;
    private final RedisTemplate redisTemplate;
    private final ChannelTopic channelTopic;
    private final JwtProvider jwtProvider;
    private final S3Service s3Service;


    public String getRoomId(String destination) {
        int lastIndex = destination.lastIndexOf('/');
        if (lastIndex != -1)
            return destination.substring(lastIndex + 1);
        else
            return "";
    }

    public ChatMessage getUserDetail(ChatMessage message, String token) {
        String nickname = jwtProvider.getNickNameFromToken(token.substring(9));
        String userId = jwtProvider.getUserIdFromToken(token.substring(9));
        String profileImageUrl = jwtProvider.getProfileImageUrlFromToken(token.substring(9));
        message.setSender(nickname);
        message.setSenderId(Long.valueOf(userId));
        message.setProfileImageUrl(profileImageUrl);
        return message;
    }


    @Transactional
    public void sendChatMessage(ChatMessage chatMessage) {
        ChatMessageEntity chatMessageEntity = new ChatMessageEntity(chatMessage);
        ChatRoomEntity chatRoomEntity = chatRoomRepository.findById(chatMessage.getRoomId())
                .orElseThrow(() -> new NotFoundException("채팅방을 찾을 수 없습니다."));

        long userCount = redisChatRepository.getUserCount(chatRoomEntity.getRoomId());
        chatMessage.setUserCount(userCount);

        chatRoomEntity.updateLastMessage(chatMessage.getMessage(), chatMessage.getSender(), chatMessage.getProfileImageUrl(), LocalDateTime.now());
        chatMessageRepository.save(chatMessageEntity);

        // 메시지를 Redis Pub/Sub 채널로 발송
        redisTemplate.convertAndSend(channelTopic.getTopic(), chatMessage);

    }


    @Transactional(readOnly = true)
    public Slice<ChatMessage> getChatMessages(User user, String roomId, Pageable pageable) {
        ChatRoomEntity chatRoomEntity = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("채팅방을 찾을 수 없습니다."));

        LocalDateTime userEntryTime = chatRoomEntity.getChatRoomparticipants().stream()
                .filter(participant -> participant.getNickname().equals(user.getNickname()))
                .findFirst()
                .map(ChatRoomParticipant::getEntryTime)
                .orElseThrow(() -> new NotFoundException("사용자가 채팅방에 참가하지 않았습니다."));

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

        return uploadedImageUrl;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    @Transactional
    public ResponseEntity<List<ChatRoom>> getUserRooms(User user) {
        Long userId = user.getId();

        List<ChatRoomEntity> chatRoomEntities = chatRoomRepository.findByChatRoomparticipants_UserId(userId);

        List<ChatRoom> userRooms = chatRoomEntities.stream()
                .map(chatRoomEntity -> {
                    ChatRoom chatRoom = redisChatRepository.findRoomById(chatRoomEntity.getRoomId());
                    chatRoom.setName(chatRoomEntity.getTitle());
                    chatRoom.setTitleImageUrl(chatRoomEntity.getTitleImageUrl());
                    chatRoom.setUserCount(redisChatRepository.getUserCount(chatRoom.getRoomId()));
                    chatRoom.setParticipants(
                            chatRoomEntity.getChatRoomparticipants().stream()
                                    .map(ChatRoomParticipant::getNickname)
                                    .collect(Collectors.toList())
                    );
                    chatRoom.setLastMessage(chatRoomEntity.getLastMessage());
                    chatRoom.setLastMessageSender(chatRoomEntity.getLastMessageSender());
                    chatRoom.setLastMessageSenderProfileImageUrl(chatRoomEntity.getLastMessageSenderProfileImageUrl());
                    chatRoom.setLastMessageTime(chatRoomEntity.getLastMessageTime());
                    return chatRoom;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(userRooms);
    }

    @Transactional
    public ChatRoomEntity createRoomByPost(PostRequestDto postRequestDto, User user) {
        ChatRoom chatRoom = redisChatRepository.createChatRoom(postRequestDto.getTitle());

        ChatRoomEntity chatRoomEntity = new ChatRoomEntity();
        chatRoomEntity.setRoomId(chatRoom.getRoomId());
        chatRoomEntity.setTitle(postRequestDto.getTitle());
        chatRoomEntity.setTitleImageUrl(postRequestDto.getImageUrlList().get(0));
        chatRoomEntity.setMasterId(user.getId());
        chatRoomEntity.setMasterNickname(user.getNickname());

        // ChatRoomParticipant 생성 및 설정
        ChatRoomParticipant participant = new ChatRoomParticipant();
        participant.setUserId(user.getId());
        participant.setNickname(user.getNickname());
        participant.setEntryTime(LocalDateTime.now());
        participant.setChatRoom(chatRoomEntity);

        chatRoomEntity.getChatRoomparticipants().add(participant);

        chatRoomRepository.save(chatRoomEntity);
        redisChatRepository.addUserToRoom(chatRoomEntity.getRoomId(), user.getNickname());

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
            List<ChatRoomParticipant> participants = chatRoomEntity.getChatRoomparticipants();
            boolean isNewParticipant = participants.stream()
                    .noneMatch(participant -> participant.getUserId().equals(newUser.getId()));

            if (isNewParticipant) {
                ChatRoomParticipant participant = new ChatRoomParticipant();
                participant.setChatRoom(chatRoomEntity);
                participant.setUserId(newUser.getId());
                participant.setNickname(newUser.getNickname());
                participant.setEntryTime(LocalDateTime.now());

                participants.add(participant);

                chatRoomRepository.save(chatRoomEntity);
                redisChatRepository.addUserToRoom(roomId, newUserNickname);
            }
        }

        return chatRoom;
    }

    public ResponseEntity<String> leaveRoomByPost(String roomId, User user) {
        ChatRoom chatRoom = redisChatRepository.findRoomById(roomId);

        if (chatRoom == null) {
            return ResponseEntity.notFound().build();
        }

        String leavingUserNickname = user.getNickname();

        ChatRoomEntity chatRoomEntity = chatRoomRepository.findById(roomId).orElse(null);
        if (chatRoomEntity != null) {
            List<ChatRoomParticipant> participants = chatRoomEntity.getChatRoomparticipants();
            ChatRoomParticipant leavingParticipant = participants.stream()
                    .filter(participant -> participant.getNickname().equals(leavingUserNickname))
                    .findFirst()
                    .orElse(null);

            if (leavingParticipant == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 권한이 없습니다.");
            }

            participants.remove(leavingParticipant);

            boolean isRoomEmpty = participants.isEmpty();

            if (isRoomEmpty) {
                chatRoomRepository.deleteById(roomId);
                redisChatRepository.deleteChatRoom(roomId);
                return ResponseEntity.ok("채팅방(" + roomId + ")에서 나갔습니다.");
            } else {
                chatRoomRepository.save(chatRoomEntity);

                if (leavingParticipant.getUserId().equals(chatRoomEntity.getMasterId())) {
                    chatRoomRepository.deleteById(roomId);
                    redisChatRepository.deleteChatRoom(roomId);
                    return ResponseEntity.ok("채팅방(" + roomId + ")이 삭제됩니다.");
                } else {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setType(ChatMessage.MessageType.QUIT);
                    chatMessage.setRoomId(roomId);
                    chatMessage.setSender("[알림]");
                    chatMessage.setMessage(leavingUserNickname + "님이 방을 나갔습니다.");
                    chatMessage.setProfileImageUrl(null);
                    sendChatMessage(chatMessage);
                    return ResponseEntity.ok("채팅방(" + roomId + ")에서 나갔습니다.");
                }
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    @Transactional
    public ChatRoom createRoom(String nickname) {
        ChatRoom chatRoom = redisChatRepository.createChatRoom(nickname);

        ChatRoomEntity chatRoomEntity = new ChatRoomEntity();
        chatRoomEntity.setRoomId(chatRoom.getRoomId());

        ChatRoomParticipant participant = new ChatRoomParticipant();
        participant.setChatRoom(chatRoomEntity);
        participant.setNickname(nickname);
        participant.setEntryTime(LocalDateTime.now());
        chatRoomEntity.getChatRoomparticipants().add(participant);

        chatRoomEntity.setTitle(nickname);
        chatRoomRepository.save(chatRoomEntity);

        return chatRoom;
    }
    @Transactional
    public ApiResponse<?> reportChat(Long messageId, ChatReportRequestDto chatReportRequestDto, List<MultipartFile> images, User user) {
        List<String> imageUrlList = s3Service.uploads(images);
        ChatMessageEntity chatMessageEntity = chatMessageRepository.findById(messageId).orElseThrow(()
                -> new IllegalArgumentException("존재하지 메세지 입니다."));
        ChatReportEntity chatReportEntity = new ChatReportEntity(user,chatMessageEntity.getSenderId(), chatMessageEntity, imageUrlList,chatReportRequestDto.getReport());
        chatMessageEntity.increaseReportCount();
        chatReportRepository.save(chatReportEntity);
        return ApiResponse.okWithMessage(CHAT_REPORT_SUCCESS);
    }
}