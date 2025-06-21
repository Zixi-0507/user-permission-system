package com.tonpower.userservice.model.dto.users;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求
 *
 * @author 程序员子曦
 * @from  
 */
@Data
public class UsersLoginRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;
    /**
     * 用户账号
     */
    private String username;
    /**
     * 用户密码
     */
    private String password;
}
