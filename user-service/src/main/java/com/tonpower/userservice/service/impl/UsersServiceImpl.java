package com.tonpower.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tonpower.userservice.exception.BusinessException;
import com.tonpower.userservice.exception.ErrorCode;
import com.tonpower.userservice.mapper.UsersMapper;

import com.tonpower.userservice.service.UsersService;
import com.tonpower.userservice.model.entity.Users;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;


/**
 * @author 86166
 * @ClassName UsersServiceImpl
 * @Description TODO
 * @date 2025-06-13 11:46
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users>
        implements UsersService {
    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "zixi";

    /**
     * 用户注册
     * @param username   用户账户
     * @param password  用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    @Override
    public long userRegister(String username, String password, String checkPassword) {
        //1. 校验
        if(StringUtils.isAnyBlank(username, password, checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if(username.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号过短");
        }
        if(password.length() < 8 || checkPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");
        }
        //密码和确认密码相同
        if(!password.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次输入的密码不一致");
        }
        //账户不能重复
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        long count = this.baseMapper.selectCount(queryWrapper);
        if(count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户已存在");
        }
        //加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        //插入数据
        Users users = new Users();
        users.setUsername(username);
        users.setPassword(encryptPassword);
        boolean save = this.save(users);
        if(!save){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"注册失败");
        }
        //TODO 绑定默认角色
        //TODO 发送注册日志
        return users.getUserId();
    }
}
