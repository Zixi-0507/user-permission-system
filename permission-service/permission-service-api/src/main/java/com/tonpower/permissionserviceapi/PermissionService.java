package com.tonpower.permissionserviceapi;

/**
 * @author 86166
 * @ClassName PermissionService
 * @Description TODO
 * @date 2025-06-15 12:48
 */
public interface PermissionService {
    // RPC接口定义
    // 绑定默认角色（普通用户）
    void bindDefaultRole(Long userId);

    // 查询用户角色码（返回role_code）
    String getUserRoleCode(Long userId);

    // 超管调用：升级用户为管理员
    void upgradeToAdmin(Long userId);

    // 超管调用：降级用户为普通角色
    void downgradeToUser(Long userId);
    String sayHello(String name);
}
