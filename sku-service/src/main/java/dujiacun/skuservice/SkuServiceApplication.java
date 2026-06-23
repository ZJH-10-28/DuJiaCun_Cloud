package dujiacun.skuservice;

import dujiacun.common.CommonResult;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackageClasses = {
        SkuServiceApplication.class,
        CommonResult.class
})
@EnableDiscoveryClient
public class SkuServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkuServiceApplication.class, args);
    }

}
