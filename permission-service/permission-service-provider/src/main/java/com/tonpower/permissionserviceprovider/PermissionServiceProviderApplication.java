package com.tonpower.permissionserviceprovider;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author 86166
 * @ClassName PermissionServiceProviderApplication
 * @Description TODO
 * @date 2025-06-15 13:47
 */

@EnableDubbo
@SpringBootApplication
public class PermissionServiceProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(PermissionServiceProviderApplication.class,args);
    }
}
