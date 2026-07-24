package dujiacun.gateway.filter;

import dujiacun.common.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import io.jsonwebtoken.Claims;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static dujiacun.common.constant.SysConstant.*;

@Slf4j
@Order(1)
@Component
public class MyGlobalFilter implements GlobalFilter {

    /**
     * 下单接口路径,仅对该写接口启用请求体防抖。
     */
    private static final String CREATE_ORDER_PATH = "/orders/orderInfo";

    /**
     * 网关防抖Key前缀。
     */
    private static final String DEBOUNCE_KEY_PREFIX = "debounce:";

    /**
     * 网关防抖时间,只用于拦截短时间重复点击。
     */
    private static final long DEBOUNCE_MILLIS = 3000;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/users/login") || path.startsWith("/users/register")) {
            return chain.filter(exchange);
        }

        String userIdGenerator = "";
        try {
            // 生成请求流水号,Redis异常时使用本地UUID兜底,该值不再承担订单业务幂等职责。
            userIdGenerator = redisTemplate.opsForValue().increment(STR_INCREMENT_ID, 1).toString();
        } catch (Exception e) {
            // Redis异常时使用本地请求标识兜底，继续执行JWT等后续校验。
            userIdGenerator = UUID.randomUUID().toString();
            log.warn("网关生成请求标识Redis异常,使用本地请求标识继续校验,userId=未解析,path={}", path, e);
        }
        //获取上下文请求
        String authHeader  = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        //黑名单校验
        try {
            if(redisTemplate.opsForValue().get(STR_BLACK_TOKEN + authHeader) != null){
                //设置401状态码
                exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        } catch (Exception e) {
            // Redis异常时跳过黑名单校验，但继续执行JWT签名和权限校验。
            log.warn("网关黑名单Redis校验异常,继续执行后续校验,userId=未解析,path={}", path, e);
        }

        //判断请求头是否正确
        if (!(authHeader != null && authHeader.startsWith(TOKEN_HEADER))){
            //设置401状态码
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        String token = authHeader.substring(7);

        //判断URL中是否有?name=dujiacun
        String name = exchange.getRequest().getQueryParams().getFirst("name");
        if(!"dujiacun".equals(name)){
            //设置401状态码
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        //JWT校验
        try{
            Claims claims = JwtUtil.parseToken(token);
            Map<String, Object> userInfo = new HashMap<>(claims);

            //客户端为admin
            if (Integer.parseInt(exchange.getRequest().getHeaders().getFirst(STR_IS_ADMIN)) == IS_ADMIN){

                if (Integer.parseInt(userInfo.get(STR_IS_ADMIN).toString()) == IS_ADMIN){

                    //将路径替换为admins
                    String newPath = exchange
                            .getRequest()
                            .getURI()
                            .getPath().replace("/users", "/admins");

                    //创建新请求对象
                    ServerHttpRequest newRequest = exchange
                            .getRequest()
                            .mutate()
                            .path(newPath)
                            .build();

                    //构建新请求
                    exchange = exchange
                            .mutate()
                            .request(newRequest)
                            .build();

                }
                else {
                    //设置403状态码
                    exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }
            ServerHttpRequest forwardedRequest = exchange.getRequest().mutate()
                    .header(STR_USER_ID, claims.getSubject())
                    .header(STR_INCREMENT_ID,userIdGenerator)
                    .build();
            exchange = exchange.mutate()
                    .request(forwardedRequest)
                    .build();

            if (isCreateOrderRequest(exchange.getRequest())) {
                return debounceCreateOrderRequest(exchange, chain, claims.getSubject());
            }

        } catch (Throwable t) {
            //设置401状态码
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    /**
     * 判断当前请求是否为需要网关防抖的下单写接口。
     */
    private boolean isCreateOrderRequest(ServerHttpRequest request) {
        return HttpMethod.POST.equals(request.getMethod())
                && CREATE_ORDER_PATH.equals(request.getURI().getPath());
    }

    /**
     * 对下单请求读取请求体并生成摘要,再使用用户ID、方法、路径和摘要做短时间防抖。
     */
    private Mono<Void> debounceCreateOrderRequest(ServerWebExchange exchange, GatewayFilterChain chain, String userId) {
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .switchIfEmpty(Mono.fromSupplier(() -> exchange.getResponse().bufferFactory().wrap(new byte[0])))
                .flatMap(dataBuffer -> {
                    byte[] bodyBytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bodyBytes);
                    DataBufferUtils.release(dataBuffer);

                    String bodyHash = buildBodyHash(bodyBytes);
                    String debounceKey = DEBOUNCE_KEY_PREFIX
                            + userId + ":"
                            + exchange.getRequest().getMethod() + ":"
                            + exchange.getRequest().getURI().getPath() + ":"
                            + bodyHash;
                    try {
                        if (Boolean.FALSE.equals(redisTemplate.opsForValue()
                                .setIfAbsent(debounceKey, "isDebounce", DEBOUNCE_MILLIS, TimeUnit.MILLISECONDS))) {
                            // 命中网关防抖时返回429,订单服务幂等仍负责最终一致性。
                            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS);
                            return exchange.getResponse().setComplete();
                        }
                    } catch (Exception e) {
                        // Redis异常时放行到订单服务幂等校验,避免网关组件故障直接阻断下单。
                        log.warn("网关防抖Redis异常,放行到订单服务幂等校验,userId={},path={}",
                                userId, exchange.getRequest().getURI().getPath(), e);
                    }

                    ServerHttpRequest decoratedRequest = rebuildRequestBody(exchange, bodyBytes);
                    ServerWebExchange decoratedExchange = exchange.mutate().request(decoratedRequest).build();
                    return chain.filter(decoratedExchange);
                });
    }

    /**
     * 重建请求体,避免Gateway读取body后下游订单服务无法再次读取。
     */
    private ServerHttpRequest rebuildRequestBody(ServerWebExchange exchange, byte[] bodyBytes) {
        Flux<DataBuffer> cachedBody = Flux.defer(() -> {
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bodyBytes);
            return Mono.just(buffer);
        });
        return new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.putAll(super.getHeaders());
                headers.setContentLength(bodyBytes.length);
                return headers;
            }

            @Override
            public Flux<DataBuffer> getBody() {
                return cachedBody;
            }
        };
    }

    /**
     * 使用SHA-256计算请求体摘要,Redis中只保存摘要参与防抖Key。
     */
    private String buildBodyHash(byte[] bodyBytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bodyBytes));
        } catch (NoSuchAlgorithmException e) {
            return UUID.nameUUIDFromBytes(bodyBytes).toString();
        }
    }
}
