访问URL:
http://localhost:12345/order/1?name=dujiacun

``` lua
DuJiaCun_Cloud
├── gateway -- 网关模块
├── feignClient -- FeignClient模块
├── order-service -- 订单服务
├── user-service -- 用户服务
├──
└── config -- 配置中心存储的配置
```
# @Transactional 事务回滚
    Spring 默认只在遇到 RuntimeException 时自动回滚事务，捕获 Exception 不会回滚 , 所以自定义异常要继承 RuntimeException

# 配置
### 网关配置
    网关的端口号是 10010
    Gateway模块中配置了 MyGlobalFilter ,要求URL后面需要有 name=dujiacun

### Nacos配置中心
    需要动态获取配置的服务需要追加配置
    只要服务需要 通过 bootstrap.yml 从 Nacos 拉取配置，就必须添加该依赖。
    若服务中存在 bootstrap.yml 且配置了 spring.cloud.nacos.config，则必须添加此依赖，否则启动会报错。


# order服务配置
#### 配置文件
    bootstrap.yml : 写Nacos链接参数,应用名,环境变量等
    application-{profile}.yml : 写本地固定配置
    nacos配置中心配置 : 数据库链接等

    //Nacos 配置中心
    implementation 'com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-config'
    //Bootstrap 支持（用于加载 bootstrap.yml）
    implementation 'org.springframework.cloud:spring-cloud-starter-bootstrap'

## Nacos config 动态配置
1.想使用Nacos动态配置的服务,都需要配置bootstrap.yaml文件

    实际生效逻辑：
        后加载的配置覆盖先加载的配置，而Nacos配置的加载顺序被设计为逻辑上“晚于”本地配置，因此优先级更高
    Nacos会按环境特异性从低到高加载配置：
        user-service.yaml（共享配置） → user-service-dev.yaml（环境专属配置）
    在Nacos配置中心正常工作时，
        user-service-dev.yaml中的同名配置会覆盖本地application-dev.yaml

2.引入 服务发现 和 配置中心

    implementation 'com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery'
    implementation 'com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-config'
    //Bootstrap 支持（用于加载 bootstrap.yml）
    implementation 'org.springframework.cloud:spring-cloud-starter-bootstrap'

3.想使用动态字段的 @Value 所在的类上需要加 @RefreshScope
    或者 将配置类注册为Bean (例:@Component)

``` java
//创建配置类
@Component
@ConfigurationProperties(prefix = "order")
@Data
public class OrderProperties {
private int orderId;
private String orderName;
private Double orderPrice;
}
```

```java
//在Controller中注入配置类
//订单服务配置:Nacos动态配置
@Autowired
private OrderProperties orderProperties;
```

```java
@GetMapping("/config")
public String getConfig() {
    return orderProperties.getOrderId() + " - " + orderProperties.getOrderName() + " - " + orderProperties.getOrderPrice();
}
```
#### @EnableDiscoveryClient
    order-serviced 的启动类中配置了 @EnableDiscoveryClient 注解
    新版spring boot 中会自动补充这个注解 , 但最好显示的标注上

#### @EnableFeignClients
    遵循“谁调用，谁添加”的原则    
    order-serviced 的启动类中配置了 @EnableFeignClients 注解
    
    @EnableFeignClients(basePackages = "dujiacun.feignclient")
    它会扫描指定包路径下所有带有 @FeignClient 注解的接口。

#### @Validated
    引入依赖
        spring-boot-starter-validation

    在Controller层 @RequestBody前面 加 @Validated
    在DTO层的字段上加 @Notnull 等注解
    可自定义 全局异常处理 
        全局异常处理通过 @RestControllerAdvice + @ExceptionHandler 实现，
        统一捕获和处理所有 Controller 层抛出的异常

#### @ControllerAdvice
    核心功能：全局控制器增强器，用于统一处理所有 Controller 的特定逻辑。
    主要用途：
        全局异常处理：捕获所有 @Controller 或 @RestController 抛出的异常
        全局数据绑定：统一预处理请求参数
        全局数据预处理：为所有 Controller 添加公共模型数据

    @ExceptionHandler(ApiException.class) 
        捕获 ApiException 类型的异常
        捕获 MethodArgumentNotValidException（由 @Validated @RequestBody 触发）
        捕获 BindException（表单参数绑定异常）

#### @Slf4j 
    依赖Lombok
    日志级别优先级
        TRACE < DEBUG < INFO < WARN < ERROR < OFF
```yaml
logging:
  level:
    # 根日志级别
    root: INFO
    
    # 指定包的日志级别
    dujiacun.orderservice: DEBUG
    dujiacun.orderservice.mapper: TRACE
    
  # 日志输出格式
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    
  # 日志文件配置
  file:
    name: logs/order-service.log

```

#### @NoArgsConstructor
    自动生成无参构造方法。
#### @AllArgsConstructor
    自动生成全参构造方法。
#### @RequiredArgsConstructor
    @Data 已隐式包含 @RequiredArgsConstructor
#### @Builder
    适用于 ≥4 个参数 且存在可选参数的场景。
    @Builder 会覆盖 @Data 生成的无参构造，导致 JSON 反序列化失败。
```java
@Builder
public class User {
    private String name;
    private int age;
    private String email;
}
```
等价于
```java
public class User {
    // ... 字段定义
    public static class UserBuilder {
        private String name;
        private int age;
        private String email;
        public UserBuilder name(String name) { this.name = name; return this; }
        public UserBuilder age(int age) { this.age = age; return this; }
        public UserBuilder email(String email) { this.email = email; return this; }
        public User build() { return new User(name, age, email); }
    }
    public static UserBuilder builder() { return new UserBuilder(); }
}
```


