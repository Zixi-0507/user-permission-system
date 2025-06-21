package com.tonpower.permissionserviceprovider.util;

import cn.hutool.core.util.StrUtil;
import com.tonpower.permissionserviceprovider.exception.BusinessException;
import com.tonpower.permissionserviceprovider.exception.ErrorCode;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.Optional;


/**
 *  IP工具类(AI辅助生成)
 */
public class IpUtils {
    public static String getIpAddress() {
        // 优先从Dubbo上下文获取（Dubbo服务调用场景）
        String dubboClientIp = Optional.ofNullable(RpcContext.getServerContext())
                .map(RpcContext::getRemoteHost)
                .orElse(null);
        if (StrUtil.isNotBlank(dubboClientIp)) {
            return dubboClientIp;
        }

        // 从HTTP请求获取（Web场景）
        try {
            HttpServletRequest request = ((ServletRequestAttributes)
                    Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            return ip.split(",")[0].trim();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "IP获取失败");
        }
    }
}
