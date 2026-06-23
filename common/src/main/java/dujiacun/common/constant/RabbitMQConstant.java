package dujiacun.common.constant;

public interface RabbitMQConstant {

    String STR_RETRY_COUNT = "retry_count";

    Integer ORDER_MAX_RETRY_COUNT = 3;

    String ORDER_EXCHANGE = "order.exchange";

    String ORDER_QUEUE = "order.queue";

    String ROUTING_KEY = "order.routingKey.success";

    String DEAD_ORDER_QUEUE = "dead.order.queue";

    String DEAD_ORDER_EXCHANGE = "dead.order.exchange";

    String DEAD_ORDER_ROUTING_KEY = "dead.order.routingKey.success";
}
