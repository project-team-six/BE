package team6.sobun.global.responseDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import team6.sobun.global.stringCode.ErrorCodeEnum;


@Getter
@NoArgsConstructor
public class ErrorResponse {

    private String message;
    private int status;

    /**
     * ErrorResponse 객체를 생성합니다.
     *
     * @param message 에러 메시지
     * @param status  HTTP 상태 코드
     */
    public ErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
    }

    /**
     * ErrorResponse 객체를 생성합니다.
     *
     * @param errorCodeEnum 에러 코드와 메시지를 포함하는 열거형 상수
     */
    public ErrorResponse(ErrorCodeEnum errorCodeEnum) {
        this(errorCodeEnum.getMessage(), errorCodeEnum.getStatus());
    }

    /**
     * ErrorResponse 객체를 생성합니다.
     *
     * @param message 에러 메시지
     * @param status  HTTP 상태 코드
     */
    public ErrorResponse(String message, HttpStatus status) {
        this(message, status.value());
    }
}
