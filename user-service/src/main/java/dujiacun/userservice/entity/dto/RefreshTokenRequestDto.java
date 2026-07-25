package dujiacun.userservice.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * refresh token刷新请求。
 */
@Data
public class RefreshTokenRequestDto {

    /**
     * 用于轮换新令牌对的一次性refresh token。
     */
    @NotBlank(message = "refresh token不能为空")
    private String refreshToken;
}
