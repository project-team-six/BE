package team6.sobun.global.exception;

import team6.sobun.global.stringCode.ErrorCodeEnum;

public class InvalidConditionException extends IllegalArgumentException {

    ErrorCodeEnum errorCodeEnum;

    public InvalidConditionException(ErrorCodeEnum errorCodeEnum) {
        this.errorCodeEnum = errorCodeEnum;
    }
}