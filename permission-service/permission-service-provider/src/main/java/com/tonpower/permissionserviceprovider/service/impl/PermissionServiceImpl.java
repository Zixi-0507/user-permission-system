package com.tonpower.permissionserviceprovider.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tonpower.permissionserviceapi.PermissionService;
import com.tonpower.permissionserviceprovider.mapper.RolesMapper;
import com.tonpower.permissionserviceprovider.mapper.UserRolesMapper;
import com.tonpower.permissionserviceprovider.model.entity.Roles;
import com.tonpower.permissionserviceprovider.model.entity.UserRoles;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
 * @author 86166
 * @ClassName PermissionServiceImpl
 * @Description TODO
 * @date 2025-06-15 13:38
 */
@DubboService(
        group = "permission-group",
        version = "1.0.0",
        timeout = 5000,
        retries = 2
)
public class PermissionServiceImpl implements PermissionService {
    @Resource
    private UserRolesMapper userRolesMapper;
    @Resource
    private RolesMapper rolesMapper;
    @Override
    public void bindDefaultRole(Long userId) {

    }

    @Override
    public String getUserRoleCode(Long userId) {
        return null;
    }

    @Override
    public void upgradeToAdmin(Long userId) {

    }

    @Override
    public void downgradeToUser(Long userId) {

    }

    @Override
    public String sayHello(String name) {
        return "hello world";
    }
}
