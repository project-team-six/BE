package team6.sobun.global.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import team6.sobun.global.stringCode.SuccessCodeEnum;

@Getter
@Builder
@AllArgsConstructor
public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final ErrorResponse error;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data) // 데이터는 data 필드에 들어가도록 변경
                .error(null) // 에러는 null로 설정
                .build();
    }

    public static <T> ApiResponse<T> error(ErrorResponse errorResponse) {
        return ApiResponse.<T>builder()
                .success(false)
                .data(null) // 데이터는 null로 설정
                .error(errorResponse)
                .build();
    }

    public static ApiResponse<?> okWithMessage(SuccessCodeEnum successCodeEnum) {
        return ApiResponse.builder()
                .success(true)
                .data(successCodeEnum.getMessage()) // 데이터는 메시지로 설정
                .error(null) // 에러는 null로 설정
                .build();
    }

}
