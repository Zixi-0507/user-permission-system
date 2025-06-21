package com.tonpower.userservice.interceptor;

import com.tonpower.userservice.common.config.JwtConfig;
import com.tonpower.userservice.exception.BusinessException;
import com.tonpower.userservice.exception.ErrorCode;
import com.tonpower.userservice.model.entity.Users;
import com.tonpower.userservice.util.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class JwtAuthInterceptor implements HandlerInterceptor {


    @Resource
    private SecretKey secretKey;
    @Resource
    private RedissonClient redissonClient;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info("JwtAuthInterceptor preHandle");
        //查看被拦截的路径
        log.info("被拦截的路径：{}", request.getRequestURI());
        // 检查是否是内部调用
        String internalCall = request.getHeader("Internal-Call");
        if ("true".equals(internalCall)) {
            // 内部调用，跳过JWT认证
            log.info("内部服务调用，跳过JWT认证");
            return true;
        }
        // 开头 如果是OPTIONS预检请求，直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }
        // 从请求头中获取token
        String token = request.getHeader(JwtConfig.JWT_HEADER);
        System.out.println("token:"+token);

        // 如果请求头中没有token，尝试从请求参数中获取
        if (StringUtils.isBlank(token)) {
            token = request.getParameter("token");
        }
        // 如果token为空，则未登录
        if (StringUtils.isBlank(token)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 如果token以Bearer 开头，则去掉前缀
        if (token.startsWith(JwtConfig.JWT_TOKEN_PREFIX)) {
            token = token.substring(JwtConfig.JWT_TOKEN_PREFIX.length());
        }

        // 验证token
        if (!JwtUtils.validateToken(token, secretKey)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "token无效或已过期");
        }
        // 从token中获取用户信息
        Long userId = JwtUtils.getUserIdFromToken(token, secretKey);
        String username = JwtUtils.getUsernameFromToken(token, secretKey);
        String userRole = JwtUtils.getUserRoleFromToken(token, secretKey);
        System.out.println(userId);
        // 校验 token 是否存在于 Redis 中（即是否为有效登录）
        RBucket<String> bucket = redissonClient.getBucket("login:user:" + userId);
        System.out.println(bucket.get());
        String storedToken = bucket.get();
        if (storedToken == null || !storedToken.equals(token)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "token不存在或已被注销");
        }
//         构建用户对象并存入请求属性中，供后续使用
        Users user = new Users();
        user.setUserId(userId);
        user.setUsername(username);
//        user.setUserRole(userRole);
        request.setAttribute("loginUser", user);
        return true;
    }


}
