package team6.sobun.domain.chat.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor  // 기본 생성자 추가
public class ChatMessageEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) // roomId는 null이 될 수 없음
    private String roomId;

    @Column(nullable = false)
    private String sender;

    @Column(nullable = false)
    private String message;


    @Builder
    public ChatMessageEntity(String roomId, String sender, String message) {
        this.roomId = roomId;
        this.sender = sender;
        this.message = message;
    }
}