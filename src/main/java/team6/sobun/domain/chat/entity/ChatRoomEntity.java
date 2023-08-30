package team6.sobun.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import team6.sobun.global.utils.Timestamped;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomEntity extends Timestamped implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(unique = true)
    private String roomId;

    private String title;

    private String titleImageUrl;

    private Long MasterId;

    private String MasterNickname;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.EAGER)
    private List<ChatRoomParticipant> chatRoomparticipants = new ArrayList<>();

    @Column(name = "last_message")
    private String lastMessage; // 가장 최근 메시지 내용

    @Column(name = "last_message_sender")
    private String lastMessageSender; // 가장 최근 메시지 보낸 사람

    @Column(name = "last_message_sender_profile_image_url")
    private String lastMessageSenderProfileImageUrl; // 가장 최근 메시지 보낸 사람

    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime; // 가장 최근 메시지 시간


    public void updateLastMessage(String message, String sender, String profileimage, LocalDateTime time) {
        this.lastMessage = message;
        this.lastMessageSender = sender;
        this.lastMessageSenderProfileImageUrl = profileimage;
        this.lastMessageTime = time;
    }
}