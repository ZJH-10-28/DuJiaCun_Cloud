package dujiacun.orderservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "order")
@Data
public class OrderProperties {
    private int orderId;
    private String orderName;
    private Double orderPrice;

}
