package dujiacun.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter MyCorsConfig() {
        // 1. 创建 CORS 配置对象
        CorsConfiguration config = new CorsConfiguration();

        // 2. 配置具体的 CORS 策略
        config.addAllowedOriginPattern("http://localhost:4200");  // 允许的路径
        config.addAllowedMethod("GET");                     // 允许的请求方法
        config.addAllowedMethod("POST");
        config.addAllowedMethod("OPTIONS");                 // 允许预检请求

        config.addAllowedHeader("Authorization");           // 允许的请求头
        config.addAllowedHeader("isAdmin");
        config.addAllowedHeader("userId");
        config.addAllowedHeader("Content-Type");
        config.setAllowCredentials(true);                   // 允许携带凭证
        config.setMaxAge(3600L);                            // 设置预检请求缓存时间 1小时

        // 3. 创建 URL 映射源，并注册配置
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());

        // 4. 将配置应用到所有路径，并注册到源
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }

}
