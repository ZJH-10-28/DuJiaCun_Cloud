package dujiacun.common.constant;

import lombok.Data;

@Data
public class UserTokenConstant {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 是否是管理员
     */
    private Integer isAdmin;
}
