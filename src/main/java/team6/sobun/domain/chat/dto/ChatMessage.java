package team6.sobun.domain.chat.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryProjection;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import team6.sobun.domain.chat.entity.ChatMessageEntity;
import team6.sobun.global.utils.Timestamped;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor  // 기본 생성자 추가
@Getter
@Setter
public class ChatMessage extends Timestamped implements Serializable {



    public enum MessageType {
        ENTER, QUIT, TALK, IMAGE
    }

    private MessageType type; // 메시지 타입
    private String roomId; // 방번호
    private Long senderId;
    private Long messageId; // 메시지 아이디

    private String sender; // 메시지 보낸사람
    @NotBlank(message = "메시지를 입력하세요.")
    private String message;
    private String imageUrl;
    private long userCount;
    private String profileImageUrl;

    @JsonIgnore
    private String lastMessage; // 가장 최근 메시지 내용
    @JsonIgnore
    private String lastMessageSender; // 가장 최근 메시지 보낸 사람
    @JsonIgnore
    private LocalDateTime lastMessageTime; // 가장 최근 메시지 시간

    @Builder
    public ChatMessage(MessageType type, String roomId, long messageId,long senderId, String sender, String message,
                       String imageUrl, long userCount, String profileImageUrl,
                       String lastMessage, String lastMessageSender, LocalDateTime lastMessageTime,
                       LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.type = type;
        this.roomId = roomId;
        this.messageId = messageId;
        this.senderId = senderId;
        this.sender = sender;
        this.message = message;
        this.imageUrl = imageUrl;
        this.userCount = userCount;
        this.profileImageUrl = profileImageUrl;
        this.lastMessage = lastMessage;
        this.lastMessageSender = lastMessageSender;
        this.lastMessageTime = lastMessageTime;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    @QueryProjection
    public ChatMessage(ChatMessageEntity chatMessageEntity) {
        this.type = chatMessageEntity.getType();
        this.roomId = chatMessageEntity.getRoomId();
        this.senderId = chatMessageEntity.getSenderId();
        this.sender = chatMessageEntity.getSender();
        this.message = chatMessageEntity.getMessage();
        this.imageUrl = chatMessageEntity.getImageUrl();
        this.userCount = chatMessageEntity.getUserCount();
        this.profileImageUrl = chatMessageEntity.getProfileImageUrl();
        this.createdAt = chatMessageEntity.getCreatedAt();
        this.modifiedAt = chatMessageEntity.getModifiedAt();
    }


    public void add(ChatMessage chatMessage) {
    }
}