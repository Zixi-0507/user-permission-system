package com.tonpower.permissionserviceprovider.enums;


import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 86166
 * @ClassName RoleEnum
 * @Description TODO
 * @date 2025-06-15 15:59
 */

public enum UserRoleEnum {
    SUPER_ADMIN(1, "super_admin", "超级管理员"),
    USER(2, "user", "普通用户"),
    ADMIN(3, "admin", "管理员");
    private final Integer roleId;
    private final String roleCode;
    private final String roleName;

    UserRoleEnum(int roleId, String roleCode, String roleName) {
        this.roleId = roleId;
        this.roleCode = roleCode;
        this.roleName = roleName;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<Integer> getRoleIds() {
        return Arrays.stream(values()).map(item -> item.roleId).collect(Collectors.toList());
    }

    /**
     * 根据 roleId 获取枚举
     *
     * @param roleId
     * @return
     */
    public static UserRoleEnum getEnumByRoleId(Integer roleId) {
        if (ObjectUtils.isEmpty(roleId)) {
            return null;
        }
        for (UserRoleEnum anEnum : UserRoleEnum.values()) {
            if (anEnum.roleId.equals(roleId)) {
                return anEnum;
            }
        }
        return null;
    }

    public static UserRoleEnum getEnumByRoleCode(String roleCode) {
        if (ObjectUtils.isEmpty(roleCode)) {
            return null;
        }
        for (UserRoleEnum anEnum : UserRoleEnum.values()) {
            if (anEnum.roleCode.equals(roleCode)) {
                return anEnum;
            }
        }
        return null;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public String getRoleName() {
        return roleName;
    }
}
