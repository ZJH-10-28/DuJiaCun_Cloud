package dujiacun.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class ThreadPoolConfig {
    @Bean(name = "orderTaskExecutor")
    public ThreadPoolTaskExecutor orderTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(11);
        executor.setMaxPoolSize(16);
        executor.setKeepAliveSeconds(60);// 最多等待60秒
        executor.setAllowCoreThreadTimeOut(true);// 允许核心线程超时
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("order-task-executor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.setWaitForTasksToCompleteOnShutdown(true); // 停机时等待任务完成
        executor.setAwaitTerminationSeconds(60); // 停机时等待任务完成最长时间

        executor.initialize();
        return executor;
    }
}
