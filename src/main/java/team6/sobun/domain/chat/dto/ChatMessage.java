package team6.sobun.domain.chat.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;
import team6.sobun.domain.chat.entity.ChatMessageEntity;
import team6.sobun.global.utils.Timestamped;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor  // 기본 생성자 추가
@Getter
@Setter
@Builder
public class ChatMessage extends Timestamped implements Serializable {



    public enum MessageType {
        ENTER, QUIT, TALK, IMAGE
    }

    private MessageType type; // 메시지 타입
    private String roomId; // 방번호
    private long messageId; // 메시지 아이디
    private String sender; // 메시지 보낸사람
    private String message; // 메시지
    private String imageUrl;
    private long userCount; // 채팅방 인원수, 채팅방 내에서 메시지가 전달될때 인원수 갱신시 사용
    private String lastMessage; // 가장 최근 메시지 내용
    private String lastMessageSender; // 가장 최근 메시지 보낸 사람
    private LocalDateTime lastMessageTime; // 가장 최근 메시지 시간

    @Builder
    public ChatMessage(MessageType type, String roomId, long messageId, String sender, String message, String imageUrl, long userCount, String lastMessage, String lastMessageSender, LocalDateTime lastMessageTime) {
        this.type = type;
        this.roomId = roomId;
        this.messageId = messageId;
        this.sender = sender;
        this.message = message;
        this.imageUrl = imageUrl;
        this.userCount = userCount;
        this.lastMessage = lastMessage;
        this.lastMessageSender = lastMessageSender;
        this.lastMessageTime = lastMessageTime;
    }

    @QueryProjection
    public ChatMessage(ChatMessageEntity chatMessageEntity) {
        this.type = chatMessageEntity.getType();
        this.roomId = chatMessageEntity.getRoomId();
        this.sender = chatMessageEntity.getSender();
        this.message = chatMessageEntity.getMessage();
        this.imageUrl = chatMessageEntity.getImageUrl();
        this.userCount = chatMessageEntity.getUserCount();
    }


    public void add(ChatMessage chatMessage) {
    }
}