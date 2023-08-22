package team6.sobun.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;
import team6.sobun.domain.chat.service.ChatService;

import java.io.Serializable;
import java.util.*;

@Getter
@Setter
public class ChatRoom implements Serializable {

    private static final long serialVersionUID = 6494678977089006639L;

    private String roomId;
    private String name;
    private long userCount;
    private List<String> participants = new ArrayList<>(); // 채팅방 인원수

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
