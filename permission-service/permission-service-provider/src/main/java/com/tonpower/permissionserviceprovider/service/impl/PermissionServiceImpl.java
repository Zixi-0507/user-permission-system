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
        userRoles.setRoleId(UserRoleEnum.USER.getRoleId());
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
    // 超管调用：升级用户为管理员
    @Override
    public void upgradeToAdmin(Long userId) {
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR);
        UserRoles userRoles = userRolesMapper.selectById(userId);
        ThrowUtils.throwIf(userRoles == null, ErrorCode.PARAMS_ERROR,"目标用户不存在");
        Roles role = rolesMapper.selectById(userRoles.getRoleId());
        ThrowUtils.throwIf(role == null, ErrorCode.PARAMS_ERROR,"目标用户角色权限获取失败");
        //要升级的用户是否是自己 TODO 这是在user-service中进行校验的条件
        // 判断要升级的用户如果是普通用户可以升级，如果是管理员或者超管就不用再升级
        if (UserRoleEnum.ADMIN.getRoleCode().equals(role.getRoleCode())
                || UserRoleEnum.SUPER_ADMIN.getRoleCode().equals(role.getRoleCode())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"用户已经是管理员或超管");
        }
        userRoles.setRoleId(UserRoleEnum.ADMIN.getRoleId());
        int update = userRolesMapper.updateById(userRoles);
        ThrowUtils.throwIf(update <= 0, ErrorCode.SYSTEM_ERROR, "升级失败");
    }
    // 超管调用：降级用户为普通角色
    @Override
    public void downgradeToUser(Long userId) {
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR);
        UserRoles userRoles = userRolesMapper.selectById(userId);
        ThrowUtils.throwIf(userRoles == null, ErrorCode.PARAMS_ERROR,"目标用户不存在");
        Roles role = rolesMapper.selectById(userRoles.getRoleId());
        ThrowUtils.throwIf(role == null, ErrorCode.PARAMS_ERROR,"目标用户角色权限获取失败");
        //要降级的用户是否是自己 TODO 这是在user-service中进行校验的条件
        // 判断要降级的用户如果是普通用户不可以再降级，如果是管理员就可以降级
        if (UserRoleEnum.USER.getRoleCode().equals(role.getRoleCode())
                || UserRoleEnum.SUPER_ADMIN.getRoleCode().equals(role.getRoleCode())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"用户已经是普通用户或超管，不能降级");
        }
        userRoles.setRoleId(UserRoleEnum.ADMIN.getRoleId());
        int update = userRolesMapper.updateById(userRoles);
        ThrowUtils.throwIf(update <= 0, ErrorCode.SYSTEM_ERROR, "升级失败");
    }
}
