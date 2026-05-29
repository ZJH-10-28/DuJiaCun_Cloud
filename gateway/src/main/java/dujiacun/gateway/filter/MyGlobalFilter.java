package dujiacun.gateway.filter;

import lombok.val;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Order(1)
@Component
public class MyGlobalFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        //获取上下文请求
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        MultiValueMap<String, String> queryParams = serverHttpRequest.getQueryParams();

        //获取参数
        String name = queryParams.getFirst("name");
        if("dujiacun".equals(name)){
            return chain.filter(exchange);
        }

        //设置401状态码
        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
