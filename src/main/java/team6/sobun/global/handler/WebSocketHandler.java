// WebSocketHandler.java
package team6.sobun.global.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import team6.sobun.domain.chat.dto.ChatMessage;
import team6.sobun.domain.chat.dto.ChatRoom;
import team6.sobun.domain.chat.service.ChatService;

@Slf4j
@RequiredArgsConstructor
@Component
public class WebSocketHandler extends TextWebSocketHandler implements MessageListener {
    private final ObjectMapper objectMapper;
    private final ChatService chatService;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // WebSocket 메시지 처리
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        // Redis 메시지 수신 처리
        String payload = new String(message.getBody());
        log.info("Received Redis message: {}", payload);

        try {
            ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);
            ChatRoom chatRoom = chatService.findRoomById(chatMessage.getRoomId());
            chatRoom.handlerActions(null, chatMessage, chatService);
        } catch (Exception e) {
            log.error("Error handling Redis message", e);
        }
    }
}
