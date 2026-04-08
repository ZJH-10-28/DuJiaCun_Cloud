package dujiacun.feignclient.openFeign;

import dujiacun.feignclient.entity.UserEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/user/{id}")
    UserEntity getUser(@PathVariable("id") Long id);
}
