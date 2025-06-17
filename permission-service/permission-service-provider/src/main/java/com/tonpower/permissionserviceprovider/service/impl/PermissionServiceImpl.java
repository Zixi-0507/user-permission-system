package com.tonpower.permissionserviceprovider.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tonpower.permissionserviceapi.PermissionService;
import com.tonpower.permissionserviceprovider.enums.UserRoleEnum;
import com.tonpower.permissionserviceprovider.exception.BusinessException;
import com.tonpower.permissionserviceprovider.exception.ErrorCode;
import com.tonpower.permissionserviceprovider.exception.ThrowUtils;
import com.tonpower.permissionserviceprovider.mapper.RolesMapper;
import com.tonpower.permissionserviceprovider.mapper.UserRolesMapper;
import com.tonpower.permissionserviceprovider.model.entity.Roles;
import com.tonpower.permissionserviceprovider.model.entity.UserRoles;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class PermissionServiceImpl extends ServiceImpl<RolesMapper, Roles> implements PermissionService {
    @Resource
    private UserRolesMapper userRolesMapper;
    @Resource
    private RolesMapper rolesMapper;

    @Override
    public void bindDefaultRole(Long userId) {
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR);
        UserRoles userRoles = new UserRoles();
        userRoles.setUserId(userId);
        userRoles.setRoleId(2);
        int insert = userRolesMapper.insert(userRoles);
        ThrowUtils.throwIf(insert <= 0, ErrorCode.SYSTEM_ERROR, "用户默认权限设置失败");
    }

    @Override
    public String getUserRoleCode(Long userId) {
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR);
        QueryWrapper<UserRoles> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        UserRoles userRoles = userRolesMapper.selectOne(queryWrapper);
        if (userRoles == null) {
            log.info("get user roleCode failed, user will not be register");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户未注册");
        }
        UserRoleEnum roleEnum = UserRoleEnum.getEnumByRoleId(userRoles.getRoleId());
        ThrowUtils.throwIf(roleEnum == null, ErrorCode.SYSTEM_ERROR, "用户权限获取失败");
        return roleEnum.getRoleCode();

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
