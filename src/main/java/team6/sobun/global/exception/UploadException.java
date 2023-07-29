package team6.sobun.global.exception;

import team6.sobun.global.stringCode.ErrorCodeEnum;

public class UploadException extends RuntimeException{
    ErrorCodeEnum errorCodeEnum;

    public UploadException(ErrorCodeEnum errorCodeEnum) {
        this.errorCodeEnum = errorCodeEnum;
    }

    public UploadException(ErrorCodeEnum errorCodeEnum, Throwable cause) {
        super(cause);
        this.errorCodeEnum = errorCodeEnum;
    }
}
