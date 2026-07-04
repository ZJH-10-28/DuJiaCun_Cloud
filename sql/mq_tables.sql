-- =========================================================
-- 1. 生产者消息表：mq_message
-- 数据库：order-service 使用的 PostgreSQL 数据库
-- 用途：保存待发送 RabbitMQ 消息，用于生产者确认、路由失败记录和后续重试补偿。
-- =========================================================

CREATE TABLE IF NOT EXISTS mq_message (
    id BIGSERIAL PRIMARY KEY,
    message_id VARCHAR(64) NOT NULL UNIQUE,
    biz_type VARCHAR(64) NOT NULL,
    biz_id VARCHAR(64) NOT NULL,
    exchange_name VARCHAR(128) NOT NULL,
    routing_key VARCHAR(128) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'INIT',
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retry_count INTEGER NOT NULL DEFAULT 5,
    next_retry_time TIMESTAMP,
    fail_reason TEXT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE mq_message IS '生产者可靠消息表，用于记录 RabbitMQ 消息发送状态和补偿重试信息。';
COMMENT ON COLUMN mq_message.id IS '自增主键。';
COMMENT ON COLUMN mq_message.message_id IS '全局唯一消息 ID，同时作为 RabbitMQ messageId/correlation id 使用。';
COMMENT ON COLUMN mq_message.biz_type IS '业务消息类型，例如 ORDER_STOCK_DEDUCT。';
COMMENT ON COLUMN mq_message.biz_id IS '业务 ID，例如订单 ID。';
COMMENT ON COLUMN mq_message.exchange_name IS 'RabbitMQ 交换机名称。';
COMMENT ON COLUMN mq_message.routing_key IS 'RabbitMQ 路由键。';
COMMENT ON COLUMN mq_message.payload IS '消息体内容。';
COMMENT ON COLUMN mq_message.status IS '消息状态：INIT=待发送，SENT=已到达交换机，FAILED=发送或路由失败，CONSUMED=已消费，DEAD=超过最大重试次数。';
COMMENT ON COLUMN mq_message.retry_count IS '当前重试次数。';
COMMENT ON COLUMN mq_message.max_retry_count IS '最大重试次数，超过后可标记为 DEAD。';
COMMENT ON COLUMN mq_message.next_retry_time IS '下一次重试时间，供定时补偿任务扫描使用。';
COMMENT ON COLUMN mq_message.fail_reason IS '最近一次发送失败或路由失败原因。';
COMMENT ON COLUMN mq_message.create_time IS '记录创建时间。';
COMMENT ON COLUMN mq_message.update_time IS '记录最后更新时间。';

CREATE INDEX IF NOT EXISTS idx_mq_message_status_retry
ON mq_message(status, next_retry_time);

CREATE INDEX IF NOT EXISTS idx_mq_message_biz
ON mq_message(biz_type, biz_id);

COMMENT ON INDEX idx_mq_message_status_retry IS '按状态和下次重试时间扫描补偿消息。';
COMMENT ON INDEX idx_mq_message_biz IS '按业务类型和业务 ID 查询消息。';

-- =========================================================
-- 3. 消费者幂等表：mq_consume_log
-- 数据库：sku-service 使用的 PostgreSQL 数据库
-- 用途：按 biz_type + biz_id 防止同一业务消息被重复消费。
-- =========================================================

CREATE TABLE IF NOT EXISTS mq_consume_log (
    id BIGSERIAL PRIMARY KEY,
    message_id VARCHAR(64) NOT NULL,
    biz_type VARCHAR(64) NOT NULL,
    biz_id VARCHAR(64) NOT NULL,
    consume_status VARCHAR(32) NOT NULL DEFAULT 'SUCCESS',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE mq_consume_log IS '消费者幂等记录表，用于防止 RabbitMQ 消息重复执行业务逻辑。';
COMMENT ON COLUMN mq_consume_log.id IS '自增主键。';
COMMENT ON COLUMN mq_consume_log.message_id IS 'RabbitMQ 消息 ID；同一业务重试时可能出现不同 messageId。';
COMMENT ON COLUMN mq_consume_log.biz_type IS '业务消息类型，例如 ORDER_STOCK_DEDUCT。';
COMMENT ON COLUMN mq_consume_log.biz_id IS '业务 ID，例如订单 ID；与 biz_type 组成业务幂等键。';
COMMENT ON COLUMN mq_consume_log.consume_status IS '消费状态；当前实现只记录成功消费。';
COMMENT ON COLUMN mq_consume_log.create_time IS '记录创建时间。';

CREATE UNIQUE INDEX IF NOT EXISTS uk_mq_consume_log_biz
ON mq_consume_log(biz_type, biz_id);

CREATE INDEX IF NOT EXISTS idx_mq_consume_log_message_id
ON mq_consume_log(message_id);

COMMENT ON INDEX uk_mq_consume_log_biz IS '业务幂等唯一索引，同一 biz_type 和 biz_id 只能成功消费一次。';
COMMENT ON INDEX idx_mq_consume_log_message_id IS '按 RabbitMQ 消息 ID 查询消费记录。';

-- =========================================================
-- 5. 死信消息表：mq_dead_letter_message
-- 数据库：sku-service 使用的 PostgreSQL 数据库
-- 用途：保存进入死信队列的消息，便于人工排查和后续补偿。
-- =========================================================

CREATE TABLE IF NOT EXISTS mq_dead_letter_message (
    id BIGSERIAL PRIMARY KEY,
    message_id VARCHAR(64),
    biz_type VARCHAR(64),
    biz_id VARCHAR(64),
    exchange_name VARCHAR(128),
    routing_key VARCHAR(128),
    queue_name VARCHAR(128) NOT NULL,
    payload TEXT NOT NULL,
    headers TEXT,
    fail_reason TEXT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE mq_dead_letter_message IS '死信消息表，用于保存进入 RabbitMQ 死信队列的消息。';
COMMENT ON COLUMN mq_dead_letter_message.id IS '自增主键。';
COMMENT ON COLUMN mq_dead_letter_message.message_id IS 'RabbitMQ 消息 ID，可能为空。';
COMMENT ON COLUMN mq_dead_letter_message.biz_type IS '业务消息类型，优先从消息头读取。';
COMMENT ON COLUMN mq_dead_letter_message.biz_id IS '业务 ID，优先从消息头读取；不存在时可使用消息体解析结果。';
COMMENT ON COLUMN mq_dead_letter_message.exchange_name IS '消息进入死信消费者时携带的交换机名称。';
COMMENT ON COLUMN mq_dead_letter_message.routing_key IS '消息进入死信消费者时携带的路由键。';
COMMENT ON COLUMN mq_dead_letter_message.queue_name IS '死信队列名称。';
COMMENT ON COLUMN mq_dead_letter_message.payload IS '死信消息体内容。';
COMMENT ON COLUMN mq_dead_letter_message.headers IS 'RabbitMQ 消息头序列化内容。';
COMMENT ON COLUMN mq_dead_letter_message.fail_reason IS '进入死信队列的原因或本地记录的失败原因。';
COMMENT ON COLUMN mq_dead_letter_message.create_time IS '记录创建时间。';

CREATE INDEX IF NOT EXISTS idx_mq_dead_letter_message_biz
ON mq_dead_letter_message(biz_type, biz_id);

CREATE INDEX IF NOT EXISTS idx_mq_dead_letter_message_create_time
ON mq_dead_letter_message(create_time);

COMMENT ON INDEX idx_mq_dead_letter_message_biz IS '按业务类型和业务 ID 查询死信消息。';
COMMENT ON INDEX idx_mq_dead_letter_message_create_time IS '按创建时间查询死信消息。';
