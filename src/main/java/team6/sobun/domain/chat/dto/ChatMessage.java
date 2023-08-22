package team6.sobun.domain.chat.dto;

import lombok.*;
import team6.sobun.global.utils.Timestamped;

import java.io.Serializable;

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

    @Builder
    public ChatMessage(MessageType type, String roomId, long messageId, String sender, String message, String imageUrl, long userCount) {
        this.type = type;
        this.roomId = roomId;
        this.messageId = messageId;
        this.sender = sender;
        this.message = message;
        this.imageUrl = imageUrl;
        this.userCount = userCount;
    }

    public void add(ChatMessage chatMessage) {
    }
}
