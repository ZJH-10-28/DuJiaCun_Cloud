package dujiacun.orderservice;

import dujiacun.feignclient.openFeign.UserClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "dujiacun.orderservice",
        "dujiacun.common"
})
@EnableFeignClients(clients = {UserClient.class})
@EnableDiscoveryClient
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

//    //发HTTP请求的工具
//    @Bean
//    @LoadBalanced // 开启负载均衡支持 否则通过user-service 找不到端口号
//    public RestTemplate restTemplate() {
//        return new RestTemplate();
//    }
}
