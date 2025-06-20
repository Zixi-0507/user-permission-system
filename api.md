# 分库分表路由逻辑设计

## 概述

用户权限管理系统采用ShardingSphere进行分库分表，主要针对用户表进行水平分片，以提高系统扩展性和性能。

## 分片策略

### 1. 分片键

系统选择`user_id`作为分片键，原因如下：
- `user_id`是业务主键，查询频率高
- `user_id`分布均匀，可以实现负载均衡
- `user_id`单调递增，避免数据热点问题

### 2. 分库策略

系统使用模运算对`user_id`进行哈希，确定数据存储的物理库：

```yaml
database-strategy:
  standard:
    sharding-column: user_id
    sharding-algorithm-name: database-line

sharding-algorithms:
  database-line:
    type: INLINE
    props:
      algorithm-expression: ds$->{user_id % 3}
```

数据库分片数量为3，分别为：
- ds0: user_db_0
- ds1: user_db_1
- ds2: user_db_2

### 3. 分表策略

当前系统未对表进行进一步分片，保留了后续扩展的可能性。用户表位于各分片数据库中，表结构保持一致。

## 配置实现

### 1. ShardingSphere配置

```yaml
spring:
  shardingsphere:
    datasource:
      names: ds0,ds1,ds2
      ds0:
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://localhost:3306/user_db_0?serverTimezone=UTC
        username: root
        password: "050704"
      ds1:
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://localhost:3306/user_db_1?serverTimezone=UTC
        username: root
        password: "050704"
      ds2:
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://localhost:3306/user_db_2?serverTimezone=UTC
        username: root
        password: "050704"
    rules:
      sharding:
        key-generators:
          snowflake:
            type: SNOWFLAKE
            props:
              worker-id: 666
        tables:
          users:
            actual-data-nodes: ds$->{0..2}.users
            key-generate-strategy:
              column: user_id
              key-generator-name: snowflake
            database-strategy:
              standard:
                sharding-column: user_id
                sharding-algorithm-name: database-line

        sharding-algorithms:
          database-line:
            type: INLINE
            props:
              algorithm-expression: ds$->{user_id % 3}
    props:
      sql-show: true
```

### 2. 分布式主键生成

系统使用雪花算法（Snowflake）生成全局唯一ID：
- 工作机器ID (worker-id): 666
- 生成策略配置在用户表的user_id列

## 分布式事务

系统整合了ShardingSphere与Seata，支持分布式事务：

```java
@ShardingSphereTransactionType(TransactionType.BASE)
@Transactional(rollbackFor = Exception.class)
public long userRegister(UserRegisterRequest userRegisterRequest, HttpServletRequest request) {
    // 业务实现...
}
```

## 路由执行流程

1. 应用程序发起SQL请求
2. ShardingSphere解析SQL，识别分片键
3. 根据分片算法确定目标数据节点
4. 将SQL路由到目标数据库执行
5. 合并多数据源的执行结果
6. 返回完整结果集

## 跨库查询优化

针对跨库查询场景，系统采用以下优化策略：

1. **避免跨库关联**：业务设计上尽量避免跨分片的关联查询
2. **冗余设计**：适当冗余关键字段，减少跨库查询
3. **结果集合并**：对于必要的跨库查询，使用ShardingSphere的结果集合并功能
4. **读写分离**：通过配置主从库，实现读写分离，减轻主库压力

## 扩容策略

当需要进行水平扩展时，系统遵循以下步骤：

1. 添加新的数据源配置
2. 调整分片算法（如修改模数）
3. 使用ShardingSphere的弹性伸缩功能进行数据迁移
4. 完成后切换到新配置

## 监控与维护

系统提供以下监控能力：

1. SQL执行情况监控：通过`sql-show: true`配置
2. 分片效果监控：定期检查各分片数据分布是否均匀
3. 慢查询监控：记录并分析执行较慢的跨分片查询

# RPC调用链路设计

## 概述

用户权限管理系统采用Apache Dubbo作为RPC框架，实现服务间的高效、可靠通信。系统通过Dubbo构建了完整的微服务调用链路，支持同步和异步调用模式。

## 服务调用架构

### 1. RPC框架选型

- **RPC框架**：Apache Dubbo 3.x
- **注册中心**：Nacos
- **序列化协议**：Triple协议（基于HTTP/2）

### 2. 服务注册与发现

系统所有微服务均通过Nacos进行服务注册与发现：

```yaml
dubbo:
  registry:
    address: nacos://localhost:8848
    register-mode: instance
```

服务注册模式使用`instance`，这是Dubbo3推荐的应用级服务发现方法，相比Dubbo2.x接口级发现具有更好的性能。

### 3. 服务调用关系

系统中主要RPC调用链路：

1. **用户服务 → 权限服务**：用户创建、权限查询等操作
2. **API网关 → 各微服务**：请求路由和负载均衡
3. **各微服务 → 日志服务**：系统操作审计

## 服务接口定义

### 1. 权限服务API

权限服务对外暴露的RPC接口定义在`permission-service-api`模块：

```java
public interface PermissionService {
    /**
     * 绑定默认角色
     * @param userId 用户ID
     */
    void bindDefaultRole(Long userId);

    /**
     * 获取用户角色代码
     * @param userId 用户ID
     * @return 角色代码
     */
    String getUserRoleCode(Long userId);
}
```

### 2. 服务提供者实现

权限服务实现类通过`@DubboService`注解暴露服务：

```java
@DubboService(
    group = "permission-group",
    version = "1.0.0",
    timeout = 5000,
    retries = 2
)
@Slf4j
public class PermissionServiceImpl extends ServiceImpl<RolesMapper, Roles> implements PermissionService {
    // 实现方法...
}
```

### 3. 服务消费者调用

用户服务通过`@DubboReference`注解引用远程服务：

```java
// 注入远程服务（Dubbo）
@DubboReference(group = "permission-group", version = "1.0.0")
private PermissionService permissionService;
```

## 协议配置

系统使用Triple协议作为默认RPC协议：

```yaml
dubbo:
  protocol:
    name: tri
    port: 20881
```

Triple协议基于HTTP/2，支持流式通信、双向流和向前兼容，较传统Dubbo协议有更好的穿透性和性能。

## 可靠性保障

### 1. 超时与重试

服务提供方配置：
```java
@DubboService(
    timeout = 5000,    // 5秒超时
    retries = 2        // 失败后重试2次
)
```





### 2. 负载均衡

Dubbo默认使用随机负载均衡算法，系统根据实际需求选择：
- **Random**：随机算法（默认）
- **RoundRobin**：轮询算法
- **LeastActive**：最少活跃调用
- **ConsistentHash**：一致性哈希

## 服务监控与治理

### 1. 应用监控

系统使用Dubbo Admin进行应用监控：
- 服务依赖关系
- 服务调用统计
- 服务调用明细

### 2. 服务治理

通过Dubbo提供的服务治理能力实现：
- 动态配置
- 服务降级
- 访问控制

### 3. QoS控制台

各微服务启用QoS（Quality of Service）控制台：

```yaml
dubbo:
  application:
    qos-enable: true
    qos-port: 22222
    qos-accept-foreign-ip: false
```

## 安全控制

1. **认证**：网关统一JWT认证
2. **鉴权**：基于角色的权限控制
3. **传输安全**：支持HTTPS/TLS加密传输

# 消息队列可靠性保障策略

## 概述

本项目定义了用户权限管理系统中消息队列的可靠性保障策略，确保分布式环境下消息的可靠传递和处理。系统采用Spring Cloud Stream与RabbitMQ结合的消息处理架构。

## 系统中的消息队列架构

### 1. 技术选型

- **消息中间件**：RabbitMQ
- **集成框架**：Spring Cloud Stream
- **消息格式**：JSON字符串

### 2. 消息流转路径

- **生产者**：用户服务(user-service)、权限服务(permission-service)
- **消费者**：日志服务(logging-service)
- **消息主题**：logging-service-topic

### 3. Spring Cloud Stream配置

#### 3.1 生产者配置 (User Service)

```yaml
spring:
  cloud:
    stream:
      bindings:
        handleMessage-out-0:
          destination: logging-service-topic
```

#### 3.2 消费者配置 (Logging Service)

```yaml
spring:
  cloud:
    stream:
      bindings:
        handleMessage-in-0:
          destination: logging-service-topic
```

## 消息可靠性保障机制

### 1. 生产者可靠性保障

#### 1.1 消息构建方式

系统使用Spring Cloud Stream的MessageBuilder构建消息，示例代码：

```java
private void sendMsg(String action, long userId, Object oldObj, Object newObj) {
    String ip = IpUtils.getIpAddress();
    
    // 获取字段变更详情
    Map<String, Object> fieldChanges = BeanDiffUtil.diff(oldObj, newObj);
    String logMessage = String.format(
            "{\"action\":\"%s\",\"userId\":%d,\"ip\":\"%s\",\"detail\":%s}",
            action, userId, ip, JSONUtil.toJsonStr(fieldChanges)
    );
    
    Message<String> streamMessage = MessageBuilder.withPayload(logMessage).build();
    streamBridge.send("handleMessage-out-0", streamMessage);
}
```

#### 1.2 消息持久化

RabbitMQ配置中通过以下机制确保消息持久化：
- 队列持久化（durable: true）
- 消息持久化（deliveryMode: 2）

### 2. 消费者可靠性保障

#### 2.1 消费者实现

系统使用Spring Cloud Stream函数式模型实现消息消费：

```java
@Bean
public Consumer<String> handleMessage() {
    return message -> {
        OperationLogs log = new OperationLogs();
        JSONObject data = JSONUtil.parseObj(message);
        String action = data.getStr("action");
        Long userId = data.getLong("userId");
        String ip = data.getStr("ip");
        //如果data中有detail字段，则保存detail字段的值
        if(data.containsKey("detail")){
            String detail=data.getStr("detail");
            log.setDetail(detail);
        }else{
            log.setDetail(message);
        }
        log.setAction(action);
        log.setUserId(userId);
        log.setIp(ip);
        
        operationLogsService.save(log);
        System.out.println("Saved log: " + message);
    };
}
```

#### 2.2 事务保障

系统通过操作与数据库事务绑定，确保消息处理与数据库操作原子性：
- 消息处理失败时，通过数据库事务回滚避免脏数据

### 3. 消息幂等性处理

#### 3.1 Redis分布式锁

系统使用Redisson实现分布式锁，确保并发环境下的幂等性：

```java
// Redisson配置
@Configuration
@ConfigurationProperties(prefix="spring.redis")
@Data
public class RedissonConfig {
    private Integer database;
    private String host;
    private Integer port;
    private Integer timeout;
    private String password;
    
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
              .setAddress("redis://"+host+":"+port)
              .setDatabase(database)
              .setPassword(password);
        return Redisson.create(config);
    }
}
```

#### 3.2 消息处理幂等策略

消息幂等性实现依赖以下机制：

1. 唯一消息标识：action + userId 组合确保消息唯一性
2. 数据库唯一索引：通过业务ID避免重复插入
3. 消息处理前的判断：检查消息是否已处理

## 高可用与容错机制

### 1. 服务发现与负载均衡

系统通过Nacos实现服务注册发现，确保消息服务高可用：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        group: DEFAULT_GROUP
        namespace: "public"
```

## 最佳实践与注意事项

1. **消息格式规范**：始终使用JSON格式，包含action、userId、ip和detail字段
2. **异常处理**：消费者处理消息时应捕获并记录所有异常，避免影响消息队列正常运行
3. **监控指标**：关注消息队列深度、消费延迟等关键指标
4. **日志记录**：记录完整的消息处理生命周期，便于问题排查
5. **定期维护**：监控死信队列和未消费消息，及时处理异常消息



