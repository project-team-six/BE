package team6.sobun.domain.chat.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessageSearchCondition {

    private String roomId;
    private LocalDateTime entryTime;

    public ChatMessageSearchCondition(String roomId, LocalDateTime entryTime) {
        this.roomId = roomId;
        this.entryTime = entryTime;
    }
}