package com.tonpower.userservice.model.dto.users;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新请求
 *
 * @author 程序员子曦
 * @from  
 */
@Data
public class UsersUpdateRequest implements Serializable {
    /**
     * id
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String profile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}