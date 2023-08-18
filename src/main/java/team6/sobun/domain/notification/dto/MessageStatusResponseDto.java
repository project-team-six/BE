package team6.sobun.domain.notification.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
public class MessageStatusResponseDto<T> {

enum MessageType {
    EXCEPTION("fail"),
    SUCCESS("success");

    private String message;

    MessageType(String message) {
        this.message = message;
    }
}

    private int statusCode;
    private MessageType message;
    private T data;

    public MessageStatusResponseDto(HttpStatus httpStatus, MessageType message, T data){
        this.statusCode = httpStatus.value();
        this.message = message;
        this.data = data;
    }

    public static <T> MessageStatusResponseDto<T> success(HttpStatus httpStatus, T data){
        return new MessageStatusResponseDto<>(httpStatus, MessageType.SUCCESS, data);
    }

    public static MessageStatusResponseDto<MessageErrorResponseDto> fail(HttpStatus httpStatus, MessageErrorResponseDto errorResponseDTO){
        return new MessageStatusResponseDto<>(httpStatus, MessageType.EXCEPTION, errorResponseDTO);
    }
}
