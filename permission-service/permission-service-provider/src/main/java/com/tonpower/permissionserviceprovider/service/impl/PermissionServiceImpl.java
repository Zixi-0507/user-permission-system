package com.tonpower.permissionserviceprovider.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tonpower.permissionserviceapi.PermissionService;
import com.tonpower.permissionserviceprovider.constant.ActionConstant;
import com.tonpower.permissionserviceprovider.enums.UserRoleEnum;
import com.tonpower.permissionserviceprovider.exception.BusinessException;
import com.tonpower.permissionserviceprovider.exception.ErrorCode;
import com.tonpower.permissionserviceprovider.exception.ThrowUtils;
import com.tonpower.permissionserviceprovider.mapper.RolesMapper;
import com.tonpower.permissionserviceprovider.mapper.UserRolesMapper;
import com.tonpower.permissionserviceprovider.model.entity.Roles;
import com.tonpower.permissionserviceprovider.model.entity.UserRoles;
import com.tonpower.permissionserviceprovider.util.BeanDiffUtil;
import com.tonpower.permissionserviceprovider.util.IpUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.shardingsphere.transaction.annotation.ShardingSphereTransactionType;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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
@AllArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<RolesMapper, Roles> implements PermissionService {
    @Resource
    private UserRolesMapper userRolesMapper;
    @Resource
    private RolesMapper rolesMapper;
    private final StreamBridge streamBridge;

    @Override
    @ShardingSphereTransactionType(TransactionType.BASE)
    public void bindDefaultRole(Long userId) {
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR);
        UserRoles userRoles = new UserRoles();
        userRoles.setUserId(userId);
        userRoles.setRoleId(UserRoleEnum.USER.getRoleId());
        int insert = userRolesMapper.insert(userRoles);
        ThrowUtils.throwIf(insert <= 0, ErrorCode.SYSTEM_ERROR, "用户默认权限设置失败");
        //发送日志消息
        String action = ActionConstant.BIND_DEFAULT_ROLE;
        sendMsg(action, userId);
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
        //发送日志消息
        String action = ActionConstant.GET_USER_ROLE_CODE;
        sendMsg(action, userId);
        ThrowUtils.throwIf(roleEnum == null, ErrorCode.SYSTEM_ERROR, "用户权限获取失败");
        return roleEnum.getRoleCode();
    }

    // 超管调用：升级用户为管理员
    @Override
    public void upgradeToAdmin(Long userId) {
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR);
        QueryWrapper<UserRoles> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        UserRoles userRoles = userRolesMapper.selectOne(queryWrapper);
        ThrowUtils.throwIf(userRoles == null, ErrorCode.PARAMS_ERROR, "目标用户不存在");
        UserRoles oldUserRoles = new UserRoles();
        BeanUtils.copyProperties(userRoles, oldUserRoles);
        Roles role = rolesMapper.selectById(userRoles.getRoleId());
        ThrowUtils.throwIf(role == null, ErrorCode.PARAMS_ERROR, "目标用户角色权限获取失败");
        //要升级的用户是否是自己 TODO 这是在user-service中进行校验的条件
        // 判断要升级的用户如果是普通用户可以升级，如果是管理员或者超管就不用再升级
        if (UserRoleEnum.ADMIN.getRoleCode().equals(role.getRoleCode())
                || UserRoleEnum.SUPER_ADMIN.getRoleCode().equals(role.getRoleCode())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户已经是管理员或超管");
        }
        userRoles.setRoleId(UserRoleEnum.ADMIN.getRoleId());
        int update = userRolesMapper.update(userRoles, queryWrapper);
        //发送日志
        String action = ActionConstant.UPGRADE_TO_ADMIN;
        sendMsg(action, userId, oldUserRoles, userRoles);
        ThrowUtils.throwIf(update <= 0, ErrorCode.SYSTEM_ERROR, "升级失败");
    }

    // 超管调用：降级用户为普通角色
    @Override
    public void downgradeToUser(Long userId) {
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR);
        QueryWrapper<UserRoles> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        UserRoles userRoles = userRolesMapper.selectOne(queryWrapper);
        ThrowUtils.throwIf(userRoles == null, ErrorCode.PARAMS_ERROR, "目标用户不存在");
        UserRoles oldUserRoles = new UserRoles();
        BeanUtils.copyProperties(userRoles, oldUserRoles);
        Roles role = rolesMapper.selectById(userRoles.getRoleId());
        ThrowUtils.throwIf(role == null, ErrorCode.PARAMS_ERROR, "目标用户角色权限获取失败");
        //要降级的用户是否是自己 TODO 这是在user-service中进行校验的条件
        // 判断要降级的用户如果是普通用户不可以再降级，如果是管理员就可以降级
        if (UserRoleEnum.USER.getRoleCode().equals(role.getRoleCode())
                || UserRoleEnum.SUPER_ADMIN.getRoleCode().equals(role.getRoleCode())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户已经是普通用户或超管，不能降级");
        }
        userRoles.setRoleId(UserRoleEnum.USER.getRoleId());
        int update = userRolesMapper.update(userRoles, queryWrapper);
        //发送日志
        String action = ActionConstant.UPGRADE_TO_USER;
        sendMsg(action, userId, oldUserRoles, userRoles);
        ThrowUtils.throwIf(update <= 0, ErrorCode.SYSTEM_ERROR, "降级失败");
    }
    // 超管调用：获取用户列表
    @Override
    public List<Long> getUserIds(Long userId) {
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR);

        String currentRoleCode = this.getUserRoleCode(userId);

        if (UserRoleEnum.SUPER_ADMIN.getRoleCode().equals(currentRoleCode)) {
            // 超管：返回所有用户 ID
            return userRolesMapper.selectList(new QueryWrapper<UserRoles>().select("user_id"))
                    .stream()
                    .map(UserRoles::getUserId)
                    .collect(Collectors.toList());
        } else if (UserRoleEnum.ADMIN.getRoleCode().equals(currentRoleCode)) {
            // 管理员：返回普通用户 + 自己
            return userRolesMapper.selectList(
                            new QueryWrapper<UserRoles>()
                                    .eq("role_id", UserRoleEnum.USER.getRoleId())
                                    .or()
                                    .eq("user_id", userId))
                    .stream()
                    .map(UserRoles::getUserId)
                    .collect(Collectors.toList());
        } else {
            // 普通用户：仅自己
            return List.of(userId);
        }
    }
    /**
     * 抽象方法，用于发送消息
     *
     * @param action
     * @param userId
     */
    private void sendMsg(String action, long userId, Object oldObj, Object newObj) {
        String ip = IpUtils.getIpAddress();

        // 获取字段变更详情
        Map<String, Object> fieldChanges = BeanDiffUtil.diff(oldObj, newObj);
        String logMessage = String.format(
                "{\"action\":\"%s\",\"userId\":%d,\"ip\":\"%s\",\"detail\":%s}",
                action, userId, ip, JSONUtil.toJsonStr(fieldChanges)
        );

        Message<String> streamMessage = MessageBuilder.withPayload(logMessage).build();
        streamBridge.send("handleMessage-out-0", streamMessage);
    }
    private void sendMsg(String action, long userId) {
        String ip = IpUtils.getIpAddress();
        String logMessage = String.format(
                "{\"action\":\"%s\",\"userId\":%d,\"ip\":\"%s\"}",
                action, userId, ip
        );
        Message<String> streamMessage = MessageBuilder.withPayload(logMessage).build();
        streamBridge.send("handleMessage-out-0", streamMessage);
    }
}
