package team6.sobun.global.exception;

import org.springframework.http.HttpStatus;
import team6.sobun.global.stringCode.ErrorCodeEnum;

public class InvalidConditionException extends IllegalArgumentException {

    ErrorCodeEnum errorCodeEnum;

    public InvalidConditionException(ErrorCodeEnum errorCodeEnum) {
        this.errorCodeEnum = errorCodeEnum;
    }

}