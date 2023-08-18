package team6.sobun.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import team6.sobun.domain.chat.dto.ChatMessage;
import team6.sobun.domain.chat.repository.RedisChatRepository;
import team6.sobun.domain.chat.service.ChatService;
import team6.sobun.domain.post.service.S3Service;
import team6.sobun.global.jwt.JwtProvider;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatController {

    private final JwtProvider jwtProvider;
    private final RedisChatRepository redisChatRepository;
    private final S3Service s3Service;
    private final ChatService chatService;


    @MessageMapping("/chat/message")
    public void message(ChatMessage message, @Header("Authorization") String token) {
        String nickname = jwtProvider.getNickNameFromToken(token);

        message.setSender(nickname);

        if (message.getImageUrl() != null) {
            try {
                // URL에서 이미지를 다운로드하고 MultipartFile로 변환
                URL imageUrl = new URL(message.getImageUrl());
                HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
                connection.setRequestMethod("GET");
                MultipartFile imageFile = new MockMultipartFile(
                        "image",
                        imageUrl.getFile(),
                        HttpURLConnection.guessContentTypeFromName(imageUrl.getFile()),
                        connection.getInputStream()
                );

                // 다운로드한 이미지를 S3에 업로드하고 이미지 URL을 가져옴
                String uploadedImageUrl = s3Service.upload(imageFile);
                message.setImageUrl(uploadedImageUrl); // 업로드된 이미지 URL 설정
            } catch (IOException e) {
                // 다운로드 및 업로드 에러 처리
                log.error("이미지 다운로드/업로드 에러: {}", e.getMessage(), e);
            }
        }

        message.setUserCount(redisChatRepository.getUserCount(message.getRoomId()));

        chatService.sendChatMessage(message);

        log.info("메시지 전송: sender={}, roomId={}, message={}", nickname, message.getRoomId(), message.getMessage());
    }

    // 업로드를 처리하는 새로운 엔드포인트
    @PostMapping("/upload/image")
    @ResponseBody
    public ResponseEntity<String> uploadImage(@RequestPart("file") MultipartFile file, @CookieValue("accessToken") String token) {
        // 이미지를 S3에 업로드하고 업로드된 이미지 URL을 가져옴
        String uploadedImageUrl = s3Service.upload(file);
        // 토큰으로부터 유저 정보를 얻어오는 부분
        String username = jwtProvider.getNickNameFromToken(token);
        log.info("토큰 제대로 들어오나?={}",token);


        return ResponseEntity.ok(uploadedImageUrl);
    }

}

