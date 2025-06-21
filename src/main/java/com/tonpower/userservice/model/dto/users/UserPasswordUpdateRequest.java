package com.tonpower.userservice.model.dto.users;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户密码更新请求
 *
 * @author 程序员子曦
 */
@Data
public class UserPasswordUpdateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（必须）
     */
    private Long userId;

    /**
     * 原始密码（旧密码）
     */
    private String oldPassword;

    /**
     * 新密码（新密码）
     */
    private String newPassword;

    /**
     * 确认新密码
     */
    private String checkPassword;
}
