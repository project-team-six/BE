package team6.sobun.domain.chat.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class ChatRoomEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private String roomId;
    @ElementCollection
    private List<String> userIds;
}