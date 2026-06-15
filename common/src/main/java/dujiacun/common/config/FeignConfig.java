package dujiacun.common.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Retryer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader("Authorization");
        String userId = request.getHeader("userId");
        if (token != null && userId != null) {
            requestTemplate.header("Authorization", token);
            requestTemplate.header("userId", userId);
        }
        //透传seata的XID
//        String xid = RootContext.getXID(); // 获取当前XID
//        if (xid != null) {
//            requestTemplate.header(RootContext.KEY_XID, xid); // 透传XID
//        }
    }

    @Bean
    public Retryer feignRetryer() {
        return Retryer.NEVER_RETRY;
    }
}
