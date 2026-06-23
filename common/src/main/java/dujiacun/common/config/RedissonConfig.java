package dujiacun.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown") // 关闭时释放连接资源
    public RedissonClient redissonClient() {
        Config config = new Config();

        // 单机模式
        config.useSingleServer()
            .setAddress("redis://192.168.0.152:6379")
            .setPassword("root")
            .setConnectionPoolSize(10)
            .setConnectionMinimumIdleSize(10)
            .setTimeout(1000);

        // 集群模式
//        config.useClusterServers()
//            .addNodeAddress(
//                    "redis://192.168.0.152:6379",
//                    "redis://192.168.1.153:6379"
//            );

        return Redisson.create(config);
    }
}
