package dujiacun.common.constant;

import org.springframework.beans.factory.annotation.Value;

public interface SysConstant {
    /**
     * token请求头名称
     */
    String TOKEN_HEADER = "Bearer ";

    /**
     * token参数名称
     */
    String STR_TOKEN = "token";

    /**
     * token请求头名称
     */
    String STR_TOKEN_HEADER = "tokenHeader";

    /**
     * session中存储的用户信息
     */
    String STR_USER_INFO = "userInfo";

    /**
     * session中存储的用户ID
     */
    String STR_USER_ID = "userId";

    /**
     * 防抖标识
     */
    String STR_USER_ID_GENERATOR  = "userIdGenerator";

    /**
     * session中存储的自增ID
     */
    String STR_INCREMENT_ID = "incrementId";

    /**
     * session中存储的用户名称
     */
    String STR_USER_NAME = "userName";

    /**
     * session中存储的isAdmin
     */
    String STR_IS_ADMIN = "isAdmin";
    /**
     * 管理员
     */
    Integer IS_ADMIN = 1;

    /**
     * 普通用户
     */
    Integer IS_USER = 0;

    /**
     * 黑名单token
     */
    String STR_BLACK_TOKEN = "blackToken:";

    /**
     * 订单幂等校验
     */
    String STR_ORDER_INCR = "orderIncr:";

    /**
     * 商品锁
     */
    String SKU_LOCK_KEY = "lock:sku:";

    /**
     * 商品信息
     */
    String STR_SKU = "sku:";

    /**
     * 预扣减锁
     */
    String REDIS_STOCK_LOCK = "REDIS_STOCK_LOCK";

    /**
     * 回滚锁
     */
    String REDIS_ROLL_BACK_LOCK = "REDIS_ROLL_BACK_LOCK";

}
