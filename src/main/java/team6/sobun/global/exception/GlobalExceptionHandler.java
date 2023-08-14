package team6.sobun.global.exception;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import team6.sobun.global.responseDto.ApiResponse;
import team6.sobun.global.utils.ResponseUtils;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static team6.sobun.global.utils.ResponseUtils.customError;
import static team6.sobun.global.utils.ResponseUtils.error;

/**
 * GG 애플리케이션의 예외 처리 핸들러 클래스입니다.
 * 각 예외 상황에 대한 예외 처리 및 응답을 관리합니다.
 */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    /**
     * IllegalArgumentException을 처리하는 예외 핸들러입니다.
     *
     * @param ie IllegalArgumentException 인스턴스
     * @return ApiResponse 객체
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(BAD_REQUEST)
    public ApiResponse<?> handleIllegalArgsException(IllegalArgumentException ie) {
        return error(ie.getMessage(), BAD_REQUEST.value());
    }

    /**
     * AmazonS3Exception을 처리하는 예외 핸들러입니다.
     *
     * @param ae AmazonS3Exception 인스턴스
     * @return ApiResponse 객체
     */
    @ExceptionHandler(AmazonS3Exception.class)
    @ResponseStatus(BAD_REQUEST)
    public ApiResponse<?> handleAmazonS3Exception(AmazonS3Exception ae) {
        return error(ae.getMessage(), BAD_REQUEST.value());
    }

    /**
     * InvalidConditionException을 처리하는 예외 핸들러입니다.
     *
     * @param e InvalidConditionException 인스턴스
     * @return ApiResponse 객체
     */
    @ExceptionHandler(InvalidConditionException.class)
    @ResponseStatus(BAD_REQUEST)
    public ApiResponse<?> handleUserException(InvalidConditionException e) {
        return customError(e.errorCodeEnum);
    }

    /**
     * UploadException을 처리하는 예외 핸들러입니다.
     *
     * @param e UploadException 인스턴스
     * @return ApiResponse 객체
     */
    @ExceptionHandler(UploadException.class)
    @ResponseStatus(BAD_REQUEST)
    public ApiResponse<?> handleUploadsException(UploadException e) {
        return customError(e.errorCodeEnum);
    }

    /**
     * MethodArgumentNotValidException을 처리하는 예외 핸들러입니다.
     *
     * @param me MethodArgumentNotValidException 인스턴스
     * @return ApiResponse 객체
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(BAD_REQUEST)
    public ApiResponse<?> handleValidationErrors(MethodArgumentNotValidException me) {
        BindingResult bindingResult = me.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        StringBuilder validMessage = new StringBuilder();
        for (FieldError fieldError : fieldErrors) {
            validMessage.append(fieldError.getDefaultMessage());
            validMessage.append(" ");
        }
        return error(String.valueOf(validMessage), BAD_REQUEST.value());
    }

}
