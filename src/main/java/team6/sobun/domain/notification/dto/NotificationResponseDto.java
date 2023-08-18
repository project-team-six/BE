package team6.sobun.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import team6.sobun.domain.notification.entity.Notification;
import team6.sobun.domain.notification.util.AlarmType;
import team6.sobun.domain.notification.util.Chrono;

@NoArgsConstructor
@Getter
@Setter
public class NotificationResponseDto {



    private Long notificationId;

    private String message;

    private Boolean readStatus;

    private AlarmType alarmType;

    private String createdAt;

    private String senderUsername;

    private String senderNickname;

    private String senderProfileImageUrl;

    private String url;


    @Builder
    public NotificationResponseDto(Long id, String message, Boolean readStatus,
                                   AlarmType alarmType, String createdAt,
                                   String senderUsername, String senderNickname, String senderProfileImageUrl,
                                   String url) {
        this.notificationId = id;
        this.message = message;
        this.readStatus = readStatus;
        this.alarmType = alarmType;
        this.senderUsername = senderUsername;
        this.senderNickname = senderNickname;
        this.senderProfileImageUrl = senderProfileImageUrl;
        this.createdAt = createdAt;
        this.url = url;
    }

    public static NotificationResponseDto create(Notification notification) {
        String createdAt = Chrono.timesAgo(notification.getCreatedAt());

        return NotificationResponseDto.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .alarmType(notification.getAlarmType())
                .readStatus(notification.getIsRead())
                .senderUsername(notification.getSenderUsername())
                .senderNickname(notification.getSenderNickname())
                .senderProfileImageUrl(notification.getSenderProfileImageUrl())
                .createdAt(createdAt)
                .url(notification.getUrl())
                .build();
    }
}
