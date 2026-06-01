package dujiacun.orderservice.service.feignClient;

import dujiacun.orderservice.entity.UserEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface FeignUserClient {
    @GetMapping("/user/{id}")
    UserEntity getUser(@PathVariable("id") Long id);
}
