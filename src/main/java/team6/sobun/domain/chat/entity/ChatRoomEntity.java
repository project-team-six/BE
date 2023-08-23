package team6.sobun.domain.chat.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class ChatRoomEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(unique = true)
    private String roomId;

    private String postTitle;

    @ElementCollection
    private List<String> nicknames = new ArrayList<>(); // 채팅방 참여한 사용자의 닉네임 목록
    public void addUser(String nickname) {
        if (!nicknames.contains(nickname)) {
            nicknames.add(nickname);
        }
    }
}

