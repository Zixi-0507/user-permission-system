package com.tonpower.userservice.model.dto.users;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新个人信息请求
 *
 * @author 程序员子曦
 * @from  
 */
@Data
public class UsersUpdateMyRequest implements Serializable {

    /**
     * 用户账户
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

    private static final long serialVersionUID = 1L;
}