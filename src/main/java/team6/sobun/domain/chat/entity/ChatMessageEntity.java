package team6.sobun.domain.chat.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor; // 추가

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor // 추가
public class ChatMessageEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", referencedColumnName = "roomId")
    private ChatRoomEntity chatRoom;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "sender", nullable = false)
    private String sender;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public ChatMessageEntity(ChatRoomEntity chatRoom, String type, String sender, String message, LocalDateTime createdAt) {
        this.chatRoom = chatRoom;
        this.type = type;
        this.sender = sender;
        this.message = message;
        this.createdAt = createdAt;
    }
}
