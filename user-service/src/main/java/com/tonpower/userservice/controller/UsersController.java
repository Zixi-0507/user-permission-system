package com.tonpower.userservice.controller;

import com.tonpower.userservice.common.BaseResponse;
import com.tonpower.userservice.common.ResultUtils;
import com.tonpower.userservice.exception.ErrorCode;
import com.tonpower.userservice.exception.ThrowUtils;
import com.tonpower.userservice.service.UsersService;
import com.tonpower.userservice.model.dto.users.UsersRegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UsersRegisterRequest usersRegisterRequest){
        ThrowUtils.throwIf(usersRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userName = usersRegisterRequest.getUserName();
        String password = usersRegisterRequest.getPassword();
        String checkPassword = usersRegisterRequest.getCheckPassword();
        long result = usersService.userRegister(userName, password, checkPassword);
        return ResultUtils.success(result);
    }
}
