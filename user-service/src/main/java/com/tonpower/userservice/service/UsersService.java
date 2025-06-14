package com.tonpower.userservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tonpower.userservice.model.entity.Users;
import com.tonpower.userservice.model.vo.LoginUserVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 86166
 * @ClassName UsersService
 * @Description TODO
 * @date 2025-06-13 11:45
 */
public interface UsersService extends IService<Users> {
    /**
     * 用户注册
     *
     * @param username   用户账户
     * @param password  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String username, String password, String checkPassword);
    /**
     * 用户登录
     *
     * @param username  用户账户
     * @param password 用户密码
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String username, String password);
    /**
     * 获取加密后的密码
     *
     * @param password
     * @return
     */
    String getEncryptPassword(String password);
    /**
     * 获得脱敏后的登录用户信息
     *
     * @param user
     * @return
     */
    LoginUserVO getLoginUserVO(Users user);
}
