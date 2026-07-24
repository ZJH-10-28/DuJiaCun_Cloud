package dujiacun.orderservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dujiacun.orderservice.entity.dto.OrderRequestDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

import static dujiacun.common.constant.SysConstant.STR_ORDER_IDEMPOTENT;

/**
 * 订单幂等服务,统一处理下单请求的幂等Key、状态流转和请求摘要。
 */
@Slf4j
@Service
public class OrderIdempotencyService {

    /**
     * 处理中状态,用于拦截正在执行的重复请求。
     */
    private static final String STATUS_PROCESSING = "PROCESSING";

    /**
     * 成功状态前缀,后面会追加订单ID和请求摘要。
     */
    private static final String STATUS_SUCCESS_PREFIX = "SUCCESS:";

    /**
     * 失败状态前缀,用于短时间记录失败请求摘要。
     */
    private static final String STATUS_FAILED_PREFIX = "FAILED:";

    /**
     * 处理中状态TTL,覆盖正常下单事务和短时间网络重试窗口。
     */
    private static final long PROCESSING_TTL_SECONDS = 120;

    /**
     * 成功状态TTL,用于支持客户端安全重试并返回同一个订单ID。
     */
    private static final long SUCCESS_TTL_SECONDS = 7 * 24 * 60 * 60;

    /**
     * 失败状态TTL,避免异常状态长期占用同一个幂等Key。
     */
    private static final long FAILED_TTL_SECONDS = 10 * 60;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 尝试进入下单处理流程,并根据已有状态返回对应幂等决策。
     */
    public IdempotencyCheckResult tryBegin(Long userId, String idempotencyKey, String requestHash) {
        String redisKey = buildRedisKey(userId, idempotencyKey);
        String processingValue = STATUS_PROCESSING + ":" + requestHash;
        Boolean locked;
        try {
            // Redis幂等异常时放行到数据库唯一索引兜底,避免Redis故障直接阻断下单。
            locked = redisTemplate.opsForValue()
                    .setIfAbsent(redisKey, processingValue, PROCESSING_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("订单幂等Redis写入处理中状态异常,放行到数据库唯一索引兜底,userId={},idempotencyKey={}",
                    userId, idempotencyKey, e);
            return IdempotencyCheckResult.allow();
        }
        if (Boolean.TRUE.equals(locked)) {
            return IdempotencyCheckResult.allow();
        }

        Object statusValue;
        try {
            // 读取已有幂等状态,用于判断重复请求、处理中请求和请求体冲突。
            statusValue = redisTemplate.opsForValue().get(redisKey);
        } catch (Exception e) {
            log.warn("订单幂等Redis读取状态异常,放行到数据库唯一索引兜底,userId={},idempotencyKey={}",
                    userId, idempotencyKey, e);
            return IdempotencyCheckResult.allow();
        }
        String status = statusValue == null ? null : statusValue.toString();
        if (status == null || status.isBlank()) {
            return IdempotencyCheckResult.processing();
        }

        if (isDifferentRequest(status, requestHash)) {
            return IdempotencyCheckResult.conflict();
        }

        Long orderId = parseSuccessOrderId(status);
        if (orderId != null) {
            return IdempotencyCheckResult.success(orderId);
        }

        if (status.startsWith(STATUS_FAILED_PREFIX)) {
            try {
                // 删除旧失败状态后重新抢占处理权,让用户可以用同一个幂等Key重试。
                redisTemplate.delete(redisKey);
            } catch (Exception e) {
                log.warn("订单幂等Redis删除失败状态异常,放行到数据库唯一索引兜底,userId={},idempotencyKey={}",
                        userId, idempotencyKey, e);
                return IdempotencyCheckResult.allow();
            }
            return retryAfterFailed(redisKey, processingValue);
        }

        return IdempotencyCheckResult.processing();
    }

    /**
     * 标记下单成功,后续重复请求可以直接返回同一个订单ID。
     */
    public void markSuccess(Long userId, String idempotencyKey, Long orderId, String requestHash) {
        String redisKey = buildRedisKey(userId, idempotencyKey);
        String successValue = STATUS_SUCCESS_PREFIX + orderId + ":" + requestHash;
        try {
            // 成功状态写入失败不影响已创建订单,重复请求仍由数据库唯一索引兜底。
            redisTemplate.opsForValue().set(redisKey, successValue, SUCCESS_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("订单幂等Redis写入成功状态异常,userId={},idempotencyKey={},orderId={}",
                    userId, idempotencyKey, orderId, e);
        }
    }

    /**
     * 标记下单失败,允许客户端稍后带相同幂等Key重新尝试。
     */
    public void markFailed(Long userId, String idempotencyKey, String requestHash) {
        String redisKey = buildRedisKey(userId, idempotencyKey);
        String failedValue = STATUS_FAILED_PREFIX + requestHash;
        try {
            // 失败状态写入失败时不覆盖原始业务异常,调用方继续按原业务结果返回。
            redisTemplate.opsForValue().set(redisKey, failedValue, FAILED_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("订单幂等Redis写入失败状态异常,userId={},idempotencyKey={}", userId, idempotencyKey, e);
        }
    }

    /**
     * 根据用户和幂等请求头生成Redis Key。
     */
    private String buildRedisKey(Long userId, String idempotencyKey) {
        return STR_ORDER_IDEMPOTENT + userId + ":" + idempotencyKey;
    }

    /**
     * 失败状态允许重新抢占处理中标识,避免一次失败导致长期无法重试。
     */
    private IdempotencyCheckResult retryAfterFailed(String redisKey, String processingValue) {
        Boolean locked;
        try {
            // 失败后重试仍通过SETNX抢占处理权,防止多个重试请求同时进入下单流程。
            locked = redisTemplate.opsForValue()
                    .setIfAbsent(redisKey, processingValue, PROCESSING_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("订单幂等Redis失败重试抢占异常,放行到数据库唯一索引兜底,redisKey={}", redisKey, e);
            return IdempotencyCheckResult.allow();
        }
        if (Boolean.TRUE.equals(locked)) {
            return IdempotencyCheckResult.allow();
        }
        return IdempotencyCheckResult.processing();
    }

    /**
     * 判断同一个幂等Key下是否提交了不同请求体。
     */
    private boolean isDifferentRequest(String status, String requestHash) {
        String oldRequestHash = parseRequestHash(status);
        return oldRequestHash != null && !oldRequestHash.equals(requestHash);
    }

    /**
     * 从状态字符串中解析请求摘要。
     */
    private String parseRequestHash(String status) {
        if (status.startsWith(STATUS_PROCESSING + ":")) {
            return status.substring((STATUS_PROCESSING + ":").length());
        }
        if (status.startsWith(STATUS_FAILED_PREFIX)) {
            return status.substring(STATUS_FAILED_PREFIX.length());
        }
        if (status.startsWith(STATUS_SUCCESS_PREFIX)) {
            int hashStartIndex = status.indexOf(':', STATUS_SUCCESS_PREFIX.length());
            return hashStartIndex < 0 ? null : status.substring(hashStartIndex + 1);
        }
        return null;
    }

    /**
     * 使用稳定JSON内容生成下单请求摘要。
     */
    public String buildRequestHash(OrderRequestDto orderRequestDto) {
        try {
            return buildRequestHash(objectMapper.writeValueAsString(orderRequestDto));
        } catch (JsonProcessingException e) {
            log.warn("生成订单请求摘要失败", e);
            throw new IllegalArgumentException("订单请求摘要生成失败");
        }
    }

    /**
     * 使用SHA-256生成请求摘要,避免Redis和数据库保存完整请求体。
     */
    public static String buildRequestHash(String requestBody) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(requestBody.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前JDK不支持SHA-256摘要算法", e);
        }
    }

    /**
     * 从成功状态中解析订单ID,非成功状态返回空。
     */
    public static Long parseSuccessOrderId(String status) {
        if (status == null || !status.startsWith(STATUS_SUCCESS_PREFIX)) {
            return null;
        }
        String successPayload = status.substring(STATUS_SUCCESS_PREFIX.length());
        String orderIdText = successPayload.contains(":")
                ? successPayload.substring(0, successPayload.indexOf(':'))
                : successPayload;
        return orderIdText.isBlank() ? null : Long.valueOf(orderIdText);
    }

    /**
     * 订单幂等检查结果,控制Controller后续是否继续创建订单。
     */
    @Getter
    public static class IdempotencyCheckResult {

        /**
         * 是否允许当前请求继续执行下单流程。
         */
        private final boolean allowCreate;

        /**
         * 是否命中已成功创建的订单。
         */
        private final boolean success;

        /**
         * 是否命中正在处理的重复请求。
         */
        private final boolean processing;

        /**
         * 是否发现同一个幂等Key提交了不同请求体。
         */
        private final boolean conflict;

        /**
         * 重复请求命中的已有订单ID。
         */
        private final Long orderId;

        /**
         * 私有构造方法,统一通过静态工厂方法创建明确语义的结果。
         */
        private IdempotencyCheckResult(boolean allowCreate, boolean success, boolean processing, boolean conflict, Long orderId) {
            this.allowCreate = allowCreate;
            this.success = success;
            this.processing = processing;
            this.conflict = conflict;
            this.orderId = orderId;
        }

        /**
         * 表示当前请求可以继续创建订单。
         */
        private static IdempotencyCheckResult allow() {
            return new IdempotencyCheckResult(true, false, false, false, null);
        }

        /**
         * 表示已有订单创建成功,当前请求直接返回原订单ID。
         */
        private static IdempotencyCheckResult success(Long orderId) {
            return new IdempotencyCheckResult(false, true, false, false, orderId);
        }

        /**
         * 表示订单仍在处理中,当前请求应提示稍后重试。
         */
        private static IdempotencyCheckResult processing() {
            return new IdempotencyCheckResult(false, false, true, false, null);
        }

        /**
         * 表示同一幂等Key绑定了不同请求内容,当前请求应拒绝。
         */
        private static IdempotencyCheckResult conflict() {
            return new IdempotencyCheckResult(false, false, false, true, null);
        }
    }
}
