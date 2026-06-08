package dujiacun.common;

import dujiacun.common.error.ErrorCode;
import lombok.Data;

@Data
public class CommonResult<T> {

    private Long code;
    private String message;
    private T data;

    public CommonResult() {}

    public CommonResult(Long code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 成功响应
    public static <T> CommonResult<T> success() {
        return new CommonResult<T>(ErrorCode.SUCCESS.getCode(), "操作成功", null);
    }
    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<T>(ErrorCode.SUCCESS.getCode(), "操作成功", data);
    }
    public static <T> CommonResult<T> success(String message) {
        return new CommonResult<T>(ErrorCode.SUCCESS.getCode(), message, null);
    }



    // 失败响应

    public static <T> CommonResult<T> error(ErrorCode errorCode) {
        return new CommonResult<T>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> CommonResult<T> error(String message) {
        return new CommonResult<T>(ErrorCode.FAILED.getCode(), message, null);
    }
    public static <T> CommonResult<T> error(Long code, String message) {
        return new CommonResult<T>(code, message, null);
    }
}

