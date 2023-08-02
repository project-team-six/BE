package team6.sobun.domain.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {
    public enum MessageType {
        ENTER, LEAVE, TALK
    }

    private MessageType type;
    private String roomId; // 채팅방 ID
    private String sender;
    private String message;
}
