package dujiacun.orderservice.service.feignClient;

import dujiacun.orderservice.entity.UserEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface FeignUserClient {
    @GetMapping("/users/{userId}")
    UserEntity getByUserId(@PathVariable("userId") Long userId);
}
