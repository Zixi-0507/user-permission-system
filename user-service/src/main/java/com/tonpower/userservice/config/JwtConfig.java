package com.tonpower.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

/**
 *@belongsProject: fengoj-backend-microservice
 *@belongsPackage: com.feng.fengojbackendgateway.config
 *@author: fgh
 *@description:
 *@version v1.0.0
 *@createTime: 2025-03-15 12:46
*/
@Configuration
public class JwtConfig implements com.tonpower.userservice.common.config.JwtConfig {
    /**
     * 创建JWT密钥
     */
    @Bean
    public SecretKey secretKey() {
        return com.tonpower.userservice.common.config.JwtConfig.createSecretKey();
    }
}
