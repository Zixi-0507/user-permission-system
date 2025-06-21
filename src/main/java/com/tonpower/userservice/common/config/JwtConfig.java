package com.tonpower.userservice.common.config;

import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 *  @author: zixi
 *
 *  @date: 2025/6/13
 *  @description: JWT配置
 */

public interface JwtConfig {

    /**
     * JWT密钥
     */
    String JWT_SECRET = "user-permission-system-backend-user-service-jwt-secret-key";

    /**
     * JWT过期时间（1天，单位：毫秒）
     */
    long JWT_EXPIRATION = 1 * 24 * 60 * 60 * 1000L;

    /**
     * JWT令牌前缀
     */
    String JWT_TOKEN_PREFIX = "Bearer ";

    /**
     * JWT请求头
     */
    String JWT_HEADER = "Authorization";

    /**
     * 创建JWT密钥
     */
    static SecretKey createSecretKey() {
        return Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    }
}
