package team6.sobun.domain.chat.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ChatRoom implements Serializable {

    private static final long serialVersionUID = 6494678977089006639L;

    private String roomId;
    private String name;
    private long userCount;
    private String titleImageUrl;
    private List<String> participants = new ArrayList<>(); // 채팅방 인원수
    private String lastMessage;
    private String lastMessageSender;
    private String lastMessageSenderProfileImageUrl;
    private LocalDateTime lastMessageTime;

    public static ChatRoom create(String name) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.roomId = UUID.randomUUID().toString();
        chatRoom.name = name;
        return chatRoom;
    }

    public void addParticipant(String participant) {
        participants.add(participant);
    }

    public List<String> getParticipants() {
        return participants;
    }

}
