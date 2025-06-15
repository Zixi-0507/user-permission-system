package com.tonpower.permissionserviceprovider.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 角色表
 * @TableName roles
 */
@TableName(value ="roles")
@Data
public class Roles {
    /**
     * 角色ID（1-超管，2-普通用户，3-管理员）
     */
    @TableId
    private Integer roleId;

    /**
     * 角色编码（super_admin/user/admin）
     */
    private String roleCode;

    /**
     * 角色名称（如"超级管理员"）
     */
    private String roleName;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 更新时间
     */
    private Date gmtModified;

    /**
     * 是否删除
     */
    private Integer isDeleted;
}