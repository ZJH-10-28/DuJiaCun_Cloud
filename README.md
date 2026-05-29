访问URL:
http://localhost:10010/order/1?name=dujiacun

``` lua
DuJiaCun_Cloud
├── gateway -- 网关模块
├── feignClient -- FeignClient模块
├── order-service -- 订单服务
├── user-service -- 用户服务
├──
└── config -- 配置中心存储的配置
```

# 配置
### 网关配置
    网关的端口号是 10010
    Gateway模块中配置了 MyGlobalFilter ,要求URL后面需要有 name=dujiacun

### Nacos配置中心
    需要动态获取配置的服务需要追加配置
    只要服务需要 通过 bootstrap.yml 从 Nacos 拉取配置，就必须添加该依赖。
    若服务中存在 bootstrap.yml 且配置了 spring.cloud.nacos.config，则必须添加此依赖，否则启动会报错。

    //Nacos 配置中心
    implementation 'com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-config'

# order服务配置
## 配置文件
    bootstrap.yml : 写Nacos链接参数,应用名,环境变量等
    application-{profile}.yml : 写本地固定配置
    nacos配置中心配置 : 数据库链接等

### @EnableDiscoveryClient
    order-serviced 的启动类中配置了 @EnableDiscoveryClient 注解
    新版spring boot 中会自动补充这个注解 , 但最好显示的标注上

### @EnableFeignClients
    遵循“谁调用，谁添加”的原则    
    order-serviced 的启动类中配置了 @EnableFeignClients 注解
    
    @EnableFeignClients(basePackages = "dujiacun.feignclient")
    它会扫描指定包路径下所有带有 @FeignClient 注解的接口。


