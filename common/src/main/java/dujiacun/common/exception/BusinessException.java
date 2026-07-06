package dujiacun.common.exception;

import dujiacun.common.error.ErrorCode;

public class BusinessException extends RuntimeException{

    private ErrorCode errorCode;

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public BusinessException (String message){
        super(message);
    }

    public BusinessException (ErrorCode errorCode){
        // 使用错误码中的中文提示作为异常消息,便于日志和统一异常处理保持一致。
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
