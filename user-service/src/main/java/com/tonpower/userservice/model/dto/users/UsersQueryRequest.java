package com.tonpower.userservice.model.dto.users;

import com.tonpower.userservice.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户查询请求
 *
 * @author 程序员子曦
 * @from  
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UsersQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long userId;



    /**
     * 用户账户
     */
    private String username;


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