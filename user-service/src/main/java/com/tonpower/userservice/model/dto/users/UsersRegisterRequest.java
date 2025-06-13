package com.tonpower.userservice.model.dto.users;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 * @author 程序员子曦
 * @from  
 */
@Data
public class UsersRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    private String userName;

    private String password;

    private String checkPassword;
}
