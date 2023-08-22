package team6.sobun.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team6.sobun.domain.notification.dto.MessageStatusResponseDto;
import team6.sobun.domain.notification.dto.NotificationResponseDto;
import team6.sobun.domain.notification.service.NotificationService;
import team6.sobun.global.security.UserDetailsImpl;

import java.util.List;

@Tag(name = "알림 관련 API", description = "SSE 연결 및 알림 조회 및 삭제")
@RestController
@RequiredArgsConstructor //생성자를 자동으로 생성
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "SSE 연결")
    @GetMapping(value = "/", produces = MediaType.TEXT_EVENT_STREAM_VALUE)///subscribe 엔드포인트로 들어오는 요청을 처리. produces 속성은 해당 메서드가 반환하는 데이터 형식을 지정
    @ResponseStatus(HttpStatus.OK)
    public SseEmitter subscribe(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
                                @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId)  {
        return notificationService.subscribe(userDetails, lastEventId);
    }
        /*  subscribe() 메서드는 불필요한 코드가 아니며, 삭제하면 클라이언트(유저)는 SSE 구독을 요청할 수 없게 됩니다.
    send() 메서드를 통해 알림 메시지를 전송할 수는 있지만,
    클라이언트(유저)는 이를 수신할 수 있는 SSE 연결이 없기 때문에 알림 메시지를 실시간으로 받을 수 없게 됩니다. */

    @Operation(summary = "알림 전체 조회")
    @GetMapping("/all")
    public MessageStatusResponseDto<List<NotificationResponseDto>> getAllNotifications(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {

        return MessageStatusResponseDto.success(HttpStatus.OK, notificationService.getAllNotifications(userDetails.getUser().getId()));
    }

    @Operation(summary = "알림 삭제")
    @DeleteMapping("/{notificationId}")
    public MessageStatusResponseDto<NotificationResponseDto> deleteNotification(@PathVariable Long notificationId,
                                                                         @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails){
        notificationService.deleteNotification(notificationId,userDetails.getUser());
        return MessageStatusResponseDto.success(HttpStatus.OK,null);
    }
}