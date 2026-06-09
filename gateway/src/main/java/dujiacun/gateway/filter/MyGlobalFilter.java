package dujiacun.gateway.filter;

import dujiacun.common.util.JwtUtil;
import org.apache.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Order(1)
@Component
public class MyGlobalFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/users/login") || path.startsWith("/users/register")) {
            return chain.filter(exchange);
        }

        //获取上下文请求
        String authHeader  = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (!(authHeader != null && authHeader.startsWith("Bearer "))){
            //设置401状态码
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return chain.filter(exchange);
        }
        String token = authHeader.substring(7);
        String name = exchange.getRequest().getQueryParams().getFirst("name");

        if(!"dujiacun".equals(name)){
            //设置401状态码
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return chain.filter(exchange);
        }

        //JWT校验
        try{
            String userId = JwtUtil.getSubject(token);
            exchange.getRequest().mutate().header("userId", userId);
            return chain.filter(exchange);
        } catch (Throwable t) {
            //设置401状态码
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
        }

        return exchange.getResponse().setComplete();
    }
}
