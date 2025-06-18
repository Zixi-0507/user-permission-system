package com.tonpower.userservice.config;

import com.tonpower.userservice.constant.ApiConstant;
import com.tonpower.userservice.interceptor.JwtAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @description: Web配置类
 * @author: zixi
 * @create: 2025/6/13
 **/
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private JwtAuthInterceptor jwtAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册JWT认证拦截器
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/**")
                // 排除不需要拦截的路径
                .excludePathPatterns(
                        ApiConstant.USER_REGISTER,
                        ApiConstant.USER_LOGIN,
                        ApiConstant.USER_LOGOUT,
                        ApiConstant.ERROR,
                        ApiConstant.V2_API_DOCS,
                        ApiConstant.V3_API_DOCS,
                        ApiConstant.SWAGGER_UI,
                        ApiConstant.SWAGGER_RESOURCES,
                        ApiConstant.DOC_HTML,
                        ApiConstant.WEBJARS,
                        ApiConstant.FAVICON_ICO
                )
                .order(Integer.MAX_VALUE);

    }
}
