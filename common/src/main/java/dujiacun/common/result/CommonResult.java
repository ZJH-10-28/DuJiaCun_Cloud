package dujiacun.common.result;

import lombok.Data;

@Data
public class CommonResult<T> {

    private Integer code;
    private String message;
    private T data;

    public CommonResult() {}

    public CommonResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 成功响应
    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<T>(200, "操作成功", data);
    }

    public static <T> CommonResult<T> success() {
        return new CommonResult<T>(200, "操作成功", null);
    }

    // 失败响应
    public static <T> CommonResult<T> error(String message) {
        return new CommonResult<T>(500, message, null);
    }

    public static <T> CommonResult<T> error(Integer code, String message) {
        return new CommonResult<T>(code, message, null);
    }
}

