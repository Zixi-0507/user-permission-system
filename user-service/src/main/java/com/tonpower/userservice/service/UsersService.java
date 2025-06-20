package com.tonpower.userservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tonpower.userservice.common.BaseResponse;
import com.tonpower.userservice.model.dto.users.UserPasswordUpdateRequest;
import com.tonpower.userservice.model.dto.users.UsersQueryRequest;
import com.tonpower.userservice.model.dto.users.UsersUpdateRequest;
import com.tonpower.userservice.model.entity.Users;
import com.tonpower.userservice.model.vo.LoginUserVO;
import com.tonpower.userservice.model.vo.UserVO;

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
     * 用户注销
     *
     * @param
     * @return
     */
    boolean userLogout();
    /**
     * 获取当前登录用户
     *
     * @param
     * @return
     */
    Users getLoginUser();

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

    /**
     * 获取用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(Users user);
    /**
     * 是否为管理员
     *
     * @param
     * @return
     */
    boolean isAdmin();
    /**
     * 是否为管理员
     *
     * @param
     * @return
     */
    boolean isAdmin(Users user);
    /**
     * 是否为超级管理员
     *
     * @param
     * @return
     */
    boolean isSuperAdmin();
    /**
     * 是否为超级管理员
     *
     * @param
     * @return
     */
    boolean isSuperAdmin(Users user);

    /**
     * 用户注销
     *
     * @param
     * @return
     */
//    boolean userLogout();
    //TODO 客户端实现


    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<Users> getQueryWrapper(UsersQueryRequest userQueryRequest);
    /**
     * 分页获取用户列表（全部）
     *
     * @param userQueryRequest
     * @return
     */
    Page<UserVO> listUserAllByPage(UsersQueryRequest userQueryRequest);
    /**
     * 分页获取用户列表（分权限）
     *
     * @param userQueryRequest
     * @return
     */
    Page<UserVO> listUserByPageWithAuth(UsersQueryRequest userQueryRequest);

    /**
     * 修改用户信息
     * @param usersUpdateRequest
     * @return
     */
    boolean updateUser(UsersUpdateRequest usersUpdateRequest);

    /**
     * 重置密码
     * @param userPasswordUpdateRequest
     * @return
     */
    boolean resetPassword(UserPasswordUpdateRequest userPasswordUpdateRequest);
}
