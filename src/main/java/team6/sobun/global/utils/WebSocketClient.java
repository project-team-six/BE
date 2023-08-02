package team6.sobun.global.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.Scanner;

public class WebSocketClient {
    private static final String WEBSOCKET_URI = "ws://localhost:8080/ws/chat"; // WebSocket 서버 주소
    private static final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
    private static StompSession stompSession;

    public static void main(String[] args) {
        // WebSocket 클라이언트 생성
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());

        // ObjectMapper 등록
        ObjectMapper objectMapper = new ObjectMapper();
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        stompClient.setMessageConverter(converter);

        // 연결 시도
        stompClient.connect(WEBSOCKET_URI, headers, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                stompSession = session;
                System.out.println("WebSocket 서버에 연결되었습니다.");

                // 채팅방 입장 요청
                enterChatRoom("room-1", "사용자1");

                // 메시지 수신 핸들러 등록
                stompSession.subscribe("/topic/room-1", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return String.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        String message = (String) payload;
                        System.out.println("받은 메시지: " + message);
                    }
                });

                // 메시지 입력받아 전송 및 나가기
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String input = scanner.nextLine();
                    if ("exit".equalsIgnoreCase(input)) {
                        leaveChatRoom("room-1", "사용자1");
                        break;
                    }
                    sendMessage("room-1", "사용자1", input);
                }

                // 연결 종료
                stompSession.disconnect();
            }
        });
    }

    private static void enterChatRoom(String roomId, String userName) {
        team6.sobun.domain.chat.dto.ChatMessage chatMessage = new team6.sobun.domain.chat.dto.ChatMessage();
        chatMessage.setType(team6.sobun.domain.chat.dto.ChatMessage.MessageType.ENTER);
        chatMessage.setRoomId(roomId);
        chatMessage.setSender(userName);

        stompSession.send("/app/chat.enter", chatMessage);
    }

    private static void leaveChatRoom(String roomId, String userName) {
        team6.sobun.domain.chat.dto.ChatMessage chatMessage = new team6.sobun.domain.chat.dto.ChatMessage();
        chatMessage.setType(team6.sobun.domain.chat.dto.ChatMessage.MessageType.LEAVE);
        chatMessage.setRoomId(roomId);
        chatMessage.setSender(userName);

        stompSession.send("/app/chat.leave", chatMessage);
    }

    private static void sendMessage(String roomId, String userName, String message) {
        team6.sobun.domain.chat.dto.ChatMessage chatMessage = new team6.sobun.domain.chat.dto.ChatMessage();
        chatMessage.setType(team6.sobun.domain.chat.dto.ChatMessage.MessageType.TALK);
        chatMessage.setRoomId(roomId);
        chatMessage.setSender(userName);
        chatMessage.setMessage(message);

        stompSession.send("/app/chat.send", chatMessage);
    }
}
