package com.tonpower.userservice.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 86166
 * @ClassName RedissonConfig
 * @Description TODO
 * @date 2025-03-26 20:19
 */
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
        // 1. 创建配置
        Config config = new Config();
        //建议与业务数据库区分开，业务用1，限流可以用2
        config.useSingleServer()
                .setAddress("redis://"+host+":"+port)
                .setDatabase(database)
                .setPassword(password);
        // 2. 创建实例

        return Redisson.create(config);
    }
}
