package dujiacun.common.error;

public enum ErrorCode {
    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    VALIDATE_FAILED(404, "参数检验失败"),
    UNAUTHORIZED(401, "暂未登录或token已经过期"),
    FORBIDDEN(403, "没有相关权限"),
    TOO_MANY_REQUESTS(429, "请勿重复提交"),
    // 服务降级表示系统触发限流或熔断,区别于普通系统异常。
    SERVICE_DEGRADED(503, "服务繁忙，请稍后再试");

    private long code;
    private String message;

    ErrorCode(long code, String message) {
        this.code = code;
        this.message = message;
    }

    public long getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
