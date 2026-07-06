package dujiacun.common.constant;

public interface RabbitMQConstant {

    // 消费重试次数在消息头中的字段名
    String STR_RETRY_COUNT = "retry_count";

    // 订单库存扣减消息最大消费重试次数
    Integer ORDER_MAX_RETRY_COUNT = 3;

    // 订单业务交换机
    String ORDER_EXCHANGE = "order.exchange";

    // 订单库存扣减队列
    String ORDER_QUEUE = "order.queue";

    // 订单库存扣减路由键
    String ROUTING_KEY = "order.routingKey.success";

    // 订单Redis库存回滚交换机
    String ORDER_ROLLBACK_EXCHANGE = "order.rollback.exchange";

    // 订单Redis库存回滚队列
    String ORDER_ROLLBACK_QUEUE = "order.rollback.queue";

    // 订单Redis库存回滚路由键
    String ROLLBACK_ROUTING_KEY = "order.routingKey.rollback";

    // 订单死信队列
    String DEAD_ORDER_QUEUE = "dead.order.queue";

    // 订单死信交换机
    String DEAD_ORDER_EXCHANGE = "dead.order.exchange";

    // 订单死信路由键
    String DEAD_ORDER_ROUTING_KEY = "dead.order.routingKey.success";

    // 订单库存扣减业务消息类型
    String MQ_BIZ_TYPE_ORDER_STOCK_DEDUCT = "ORDER_STOCK_DEDUCT";

    // 订单Redis库存回滚业务消息类型
    String MQ_BIZ_TYPE_ORDER_STOCK_ROLLBACK = "ORDER_STOCK_ROLLBACK";

    // 订单消息ID前缀
    String MQ_MESSAGE_ID_ORDER_PREFIX = "order:";

    // 订单Redis库存回滚消息ID前缀
    String MQ_MESSAGE_ID_ROLLBACK_PREFIX = "rollback:";

    // MQ消息头:业务类型
    String MQ_HEADER_BIZ_TYPE = "biz_type";

    // MQ消息头:业务ID
    String MQ_HEADER_BIZ_ID = "biz_id";

    // 生产者消息状态:待发送
    String MQ_STATUS_INIT = "INIT";

    // 生产者消息状态:已到达交换机
    String MQ_STATUS_SENT = "SENT";

    // 生产者消息状态:发送失败或路由失败
    String MQ_STATUS_FAILED = "FAILED";

    // 生产者消息状态:已消费
    String MQ_STATUS_CONSUMED = "CONSUMED";

    // 生产者消息状态:超过最大重试次数或进入死信
    String MQ_STATUS_DEAD = "DEAD";

    // 消费者幂等记录状态:消费成功
    String MQ_CONSUME_STATUS_SUCCESS = "SUCCESS";

    // 死信消息默认失败原因
    String MQ_DEAD_LETTER_REASON = "Message entered dead letter queue";
}
