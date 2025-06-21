package com.tonpower.userservice.model.dto.users;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建请求
 *
 * @author 程序员子曦
 * @from  
 */
@Data
public class UsersAddRequest implements Serializable {

    /**
     * 用户账户
     */
    private String username;


    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户角色: user, admin, ban
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}