package com.tonpower.userservice.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 用户
 * @TableName users
 */
@TableName(value ="users")
@Data
public class Users {
    /**
     * 用户id
     */
    @TableId
    private Long userId;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 
     */
    private String email;

    /**
     * 
     */
    private String phone;

    /**
     * 密码
     */
    private String password;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

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