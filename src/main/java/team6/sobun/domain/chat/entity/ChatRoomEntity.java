package team6.sobun.domain.chat.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "chat_room_entity", indexes = @Index(columnList = "roomId")) // 인덱스 추가
public class ChatRoomEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomId;
    private String name;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "chatRoom")
    private List<ChatMessageEntity> messages;

    @Builder
    public ChatRoomEntity(String roomId, String name, LocalDateTime createdAt) {
        this.roomId = roomId;
        this.name = name;
        this.createdAt = createdAt;
    }

    public ChatRoomEntity() {
    }
}
