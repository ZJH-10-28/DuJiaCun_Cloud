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

}
