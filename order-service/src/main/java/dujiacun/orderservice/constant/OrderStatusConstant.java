package dujiacun.orderservice.constant;

public interface OrderStatusConstant {
    /**
     * 订单状态: 0-待付款
     */
    public Integer ORDER_STATUS_WAIT_FOR_PAY = 0;
    /**
     * 订单状态: 1-待发货
     */
    public Integer ORDER_STATUS_WAIT_FOR_DELIVERY = 1;
    /**
     * 订单状态: 2-待收货
     */
    public Integer ORDER_STATUS_WAIT_FOR_RECEIVE = 2;
    /**
     * 订单状态: 3-待评价
     */
    public Integer ORDER_STATUS_WAIT_FOR_COMMENT = 3;
    /**
     * 订单状态: 4-已完成
     */
    public Integer ORDER_STATUS_COMPLETED = 4;
    /**
     * 订单状态: 5-已取消
     */
    public Integer ORDER_STATUS_CANCELED = 5;
}
