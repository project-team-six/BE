package team6.sobun.domain.notification.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import team6.sobun.global.stringCode.ErrorCodeEnum;

@Getter
@NoArgsConstructor
public class MessageErrorResponseDto {
    ErrorCodeEnum errorType;

    String errorMessage;

    public void MessageErrorResponseDTO(ErrorCodeEnum errorType, String errorMessage) {
        this.errorType = errorType;
        this.errorMessage = errorMessage;
    }
}
