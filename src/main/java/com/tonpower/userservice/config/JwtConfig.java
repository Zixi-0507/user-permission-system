package com.tonpower.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

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
