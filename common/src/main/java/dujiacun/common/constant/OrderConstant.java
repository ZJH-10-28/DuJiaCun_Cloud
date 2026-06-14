package dujiacun.common.constant;

public interface OrderConstant {
    /**
     * 订单状态: 0-待付款
     */
    Integer ORDER_STATUS_WAIT_FOR_PAY = 0;
    /**
     * 订单状态: 1-待发货
     */
    Integer ORDER_STATUS_WAIT_FOR_DELIVERY = 1;
    /**
     * 订单状态: 2-待收货
     */
    Integer ORDER_STATUS_WAIT_FOR_RECEIVE = 2;
    /**
     * 订单状态: 3-待评价
     */
    Integer ORDER_STATUS_WAIT_FOR_COMMENT = 3;
    /**
     * 订单状态: 4-已完成
     */
    Integer ORDER_STATUS_COMPLETED = 4;
    /**
     * 订单状态: 5-已取消
     */
    Integer ORDER_STATUS_CANCELED = 5;

}
