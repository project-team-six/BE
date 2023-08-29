package team6.sobun.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import team6.sobun.domain.chat.dto.ChatMessage;
import team6.sobun.global.utils.Timestamped;

import java.io.Serializable;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageEntity extends Timestamped implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ChatMessage.MessageType type;

    private String roomId;

    private String sender;

    private String message;

    private String imageUrl;

    private Long userCount;


    public ChatMessageEntity(ChatMessage chatMessage) {
        this.type = chatMessage.getType();
        this.roomId = chatMessage.getRoomId();
        this.sender = chatMessage.getSender();
        this.message = chatMessage.getMessage();
        this.imageUrl = chatMessage.getImageUrl();
        this.userCount = chatMessage.getUserCount();
    }

    public ChatMessage toDto() {
        return ChatMessage.builder()
                .type(type)
                .roomId(roomId)
                .sender(sender)
                .message(message)
                .imageUrl(imageUrl)
                .userCount(userCount)
                .build();
    }
    public static ChatMessageEntity fromDto(ChatMessage chatMessage) {
        return new ChatMessageEntity(chatMessage);
    }
}