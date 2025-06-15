package com.tonpower.userservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tonpower.userservice.config.JwtConfig;
import com.tonpower.userservice.exception.BusinessException;
import com.tonpower.userservice.exception.ErrorCode;
import com.tonpower.userservice.mapper.UsersMapper;
import com.tonpower.userservice.model.entity.Users;
import com.tonpower.userservice.model.vo.LoginUserVO;
import com.tonpower.userservice.service.UsersService;
import com.tonpower.userservice.util.JwtUtils;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.crypto.SecretKey;
import java.util.HashMap;


/**
 * @author 86166
 * @ClassName UsersServiceImpl
 * @Description TODO
 * @date 2025-06-13 11:46
 */
@Service
@Slf4j
@GlobalTransactional
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users>
        implements UsersService {
    @Resource
    private JwtConfig jwtConfig;

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
        String encryptPassword = getEncryptPassword(password);
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
    /**
     * 用户登录
     * @param username   用户账户
     * @param password  用户密码
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVO userLogin(String username, String password) {
        //1. 校验
        if(StrUtil.hasBlank(username, password)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if(username.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号过短");
        }
        if(password.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");
        }
        //2. 对用户密码加密
        String encryptPassword = getEncryptPassword(password);
        //3. 查询用户是否存在
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("password", encryptPassword);
        Users user = this.baseMapper.selectOne(queryWrapper);
        if(user == null){
            log.info("user login failed, username cannot match password");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在或密码错误");
        }
        //TODO 通过远程调用获取用户角色
        //TODO 生成JWT令牌
        String userRole = "user"; // 示例角色，实际应从数据库获取或远程调用

        // 构造 claims
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("id", user.getUserId());
        claims.put("username", user.getUsername());
        claims.put("userRole", userRole);
        SecretKey secretKey= jwtConfig.secretKey();
        String token = JwtUtils.generateToken(claims, user.getUsername(), secretKey);
        LoginUserVO loginUserVO = getLoginUserVO(user);
        loginUserVO.setToken(token);
        return loginUserVO;
    }
    /**
     * 获取加密后的密码
     *
     * @param password 用户密码
     * @return 加密后的密码
     */
    @Override
    public String getEncryptPassword(String password) {
        // 加盐，混淆密码
        final String SALT = "zixi";
        return DigestUtils.md5DigestAsHex((SALT + password).getBytes());
    }
    /**
     * 获取脱敏类的用户信息
     *
     * @param user 用户
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVO getLoginUserVO(Users user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }
}
