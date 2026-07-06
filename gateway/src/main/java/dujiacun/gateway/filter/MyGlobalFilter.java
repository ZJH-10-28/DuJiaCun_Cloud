package dujiacun.gateway.filter;

import dujiacun.common.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import io.jsonwebtoken.Claims;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static dujiacun.common.constant.SysConstant.*;

@Slf4j
@Order(1)
@Component
public class MyGlobalFilter implements GlobalFilter {


    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/users/login") || path.startsWith("/users/register")) {
            return chain.filter(exchange);
        }

        //防抖
        String userIdGenerator = "";
        String userId = exchange.getRequest().getHeaders().getFirst(STR_USER_ID);
        if (userId == null || userId.isBlank()) {
            // 用户ID为空时直接拦截，避免匿名请求共用同一个防抖Key。
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        String params = exchange.getRequest().getQueryParams().toSingleValueMap().toString();
        String STR_DEBOUNCE_KEY = "debounce:";
        String debounceKey = String.format(STR_DEBOUNCE_KEY + "%s:%s:%s", userId , path , params);
        long debounceMiles = 3000;

        try {
            if (Boolean.FALSE.equals(redisTemplate.opsForValue()
                    .setIfAbsent(debounceKey, "isDebounce", debounceMiles, TimeUnit.MILLISECONDS))){
                //设置429状态码
                exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS);
                return exchange.getResponse().setComplete();
            }
        } catch (Exception e) {
            // Redis异常时继续后续鉴权校验，避免防抖组件故障阻断正常登录态请求。
            log.warn("网关防抖Redis异常,继续执行后续校验,userId={},path={}", userId, path, e);
        }

        try {
            // 生成订单幂等标识，Redis异常时使用本地UUID兜底，保证后续服务能拿到incrementId。
            userIdGenerator = redisTemplate.opsForValue().increment(STR_INCREMENT_ID, 1).toString();
        } catch (Exception e) {
            // Redis异常时使用本地请求标识兜底，继续执行JWT等后续校验。
            userIdGenerator = UUID.randomUUID().toString();
            log.warn("网关生成请求标识Redis异常,使用本地请求标识继续校验,userId={},path={}", userId, path, e);
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
            log.warn("网关黑名单Redis校验异常,继续执行后续校验,userId={},path={}", userId, path, e);
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

        } catch (Throwable t) {
            //设置401状态码
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }
}
