package com.tonpower.userservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tonpower.userservice.common.BaseResponse;
import com.tonpower.userservice.common.ResultUtils;
import com.tonpower.userservice.exception.BusinessException;
import com.tonpower.userservice.exception.ErrorCode;
import com.tonpower.userservice.exception.ThrowUtils;
import com.tonpower.userservice.model.dto.users.*;
import com.tonpower.userservice.model.entity.Users;
import com.tonpower.userservice.model.vo.LoginUserVO;
import com.tonpower.userservice.model.vo.UserVO;
import com.tonpower.userservice.service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 86166
 * @ClassName UsersController
 * @Description TODO
 * @date 2025-06-13 12:26
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UsersController {
    @Autowired
    private UsersService usersService;
    /**
     * 用户注册
     *
     * @param usersRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UsersRegisterRequest usersRegisterRequest){
        ThrowUtils.throwIf(usersRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userName = usersRegisterRequest.getUserName();
        String password = usersRegisterRequest.getPassword();
        String checkPassword = usersRegisterRequest.getCheckPassword();
        long result = usersService.userRegister(userName, password, checkPassword);
        return ResultUtils.success(result);
    }
    /**
     * 用户登录
     *
     * @param usersLoginRequest
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UsersLoginRequest usersLoginRequest){
        ThrowUtils.throwIf(usersLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String username = usersLoginRequest.getUsername();
        String password = usersLoginRequest.getPassword();
        LoginUserVO loginUserVO = usersService.userLogin(username, password);
        return ResultUtils.success(loginUserVO);
    }
    /**
     * 用户注销
     *
     * @param
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout() {
        boolean result = usersService.userLogout();
        return ResultUtils.success(result);
    }
    /**
     * 获取当前登录用户
     *
     *
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<UserVO> getLoginUser() {
        Users loginUser = usersService.getLoginUser();
        return ResultUtils.success(usersService.getUserVO(loginUser));
    }

    /**
     * 获取所有用户信息
     * @param usersQueryRequest
     * @return
     */
    @GetMapping("/list/all")
    public BaseResponse<Page<UserVO>> listUserAllByPage(UsersQueryRequest usersQueryRequest){
        long current = usersQueryRequest.getCurrent();
        long pageSize = usersQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        Page<UserVO> userVOPage = usersService.listUserAllByPage(usersQueryRequest);
        return ResultUtils.success(userVOPage);
    }
    /**
     * 分页获取用户信息
     * @param usersQueryRequest
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<UserVO>> listUserByPage(UsersQueryRequest usersQueryRequest){
        Page<UserVO> usersPage = usersService.listUserByPageWithAuth(usersQueryRequest);
        return ResultUtils.success(usersPage);
    }
    /**
     * 更新用户信息
     * @param usersUpdateRequest
     * @return
     */

    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UsersUpdateRequest usersUpdateRequest){
        boolean updateUserResult = usersService.updateUser(usersUpdateRequest);
        return ResultUtils.success(updateUserResult);
    }
    /**
     * 重置密码
     * @param userPasswordUpdateRequest
     * @return
     */
    @PostMapping("/reset-password")
    public BaseResponse<Boolean> resetPassword(@RequestBody UserPasswordUpdateRequest userPasswordUpdateRequest){
        boolean resetPasswordResult = usersService.resetPassword(userPasswordUpdateRequest);
        return ResultUtils.success(resetPasswordResult);
    }


}
