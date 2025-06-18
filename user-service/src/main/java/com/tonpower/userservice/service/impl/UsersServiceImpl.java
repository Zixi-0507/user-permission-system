package com.tonpower.userservice.service.impl;
import java.util.Date;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tonpower.permissionserviceapi.PermissionService;
import com.tonpower.userservice.config.JwtConfig;
import com.tonpower.userservice.constant.ActionConstant;
import com.tonpower.userservice.constant.CommonConstant;
import com.tonpower.userservice.enums.UserRoleEnum;
import com.tonpower.userservice.exception.BusinessException;
import com.tonpower.userservice.exception.ErrorCode;
import com.tonpower.userservice.exception.ThrowUtils;
import com.tonpower.userservice.mapper.UsersMapper;
import com.tonpower.userservice.model.dto.users.UserPasswordUpdateRequest;
import com.tonpower.userservice.model.dto.users.UsersQueryRequest;
import com.tonpower.userservice.model.dto.users.UsersUpdateRequest;
import com.tonpower.userservice.model.entity.Users;
import com.tonpower.userservice.model.vo.LoginUserVO;
import com.tonpower.userservice.model.vo.UserVO;
import com.tonpower.userservice.service.UsersService;
import com.tonpower.userservice.util.BeanDiffUtil;
import com.tonpower.userservice.util.IpUtils;
import com.tonpower.userservice.util.JwtUtils;
import com.tonpower.userservice.util.SqlUtils;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author 86166
 * @ClassName UsersServiceImpl
 * @Description TODO
 * @date 2025-06-13 11:46
 */
@Service
@Slf4j

@RequiredArgsConstructor
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users>
        implements UsersService {
    @Resource
    private JwtConfig jwtConfig;
    private final StreamBridge streamBridge;
    // 注入远程服务（Dubbo）
    @DubboReference(group = "permission-group", version = "1.0.0")
    private PermissionService permissionService;

    /**
     * 用户注册
     *
     * @param username      用户账户
     * @param password      用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    @Override
    @GlobalTransactional(timeoutMills = 300000, name = "user-service-tx")
    public long userRegister(String username, String password, String checkPassword) {

        //1. 校验
        if (StringUtils.isAnyBlank(username, password, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (username.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (password.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        //密码和确认密码相同
        if (!password.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        //账户不能重复
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已存在");
        }
        //加密
        String encryptPassword = getEncryptPassword(password);
        //插入数据
        Users user = new Users();
        user.setUsername(username);
        user.setPassword(encryptPassword);
        boolean save = this.save(user);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }
        long userId = user.getUserId();
        //TODO 绑定默认角色
        permissionService.bindDefaultRole(userId);
        log.info("用户注册成功，用户id：{}", userId);
        //发送注册日志
        String action = ActionConstant.REGISTER;
        Users newUser = new Users();
        newUser.setUserId(0L);
        newUser.setUsername("");
        newUser.setEmail("");
        newUser.setPhone("");
        newUser.setPassword("");
        newUser.setUserAvatar("");
        newUser.setUserProfile("");
        newUser.setGmtCreate(new Date());
        newUser.setGmtModified(new Date());
        newUser.setIsDeleted(0);
//        String w= String.valueOf(1/0);
        sendMsg(action, userId,newUser, user);
        return userId;
    }


    /**
     * 用户登录
     *
     * @param username 用户账户
     * @param password 用户密码
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVO userLogin(String username, String password) {
        //1. 校验
        if (StrUtil.hasBlank(username, password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (username.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (password.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        //2. 对用户密码加密
        String encryptPassword = getEncryptPassword(password);
        //3. 查询用户是否存在
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("password", encryptPassword);
        Users user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("user login failed, username cannot match password");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        long userId = user.getUserId();
        //TODO 通过远程调用获取用户角色
        String userRole = permissionService.getUserRoleCode(userId);
        //TODO 生成JWT令牌
        // 构造 claims
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("id", userId);
        claims.put("username", user.getUsername());
        claims.put("userRole", userRole);
        SecretKey secretKey = jwtConfig.secretKey();
        String token = JwtUtils.generateToken(claims, user.getUsername(), secretKey);
        LoginUserVO loginUserVO = getLoginUserVO(user);
        loginUserVO.setUserRole(userRole);
        loginUserVO.setToken(token);
        //发送登录日志
//        String action = ActionConstant.LOGIN;
//        sendMsg(action, userId);
        return loginUserVO;
    }

    /**
     * 获取当前登录用户
     *
     * @return 当前登录用户
     */
    @Override
    public Users getLoginUser() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请求上下文为空");
        }
        HttpServletRequest request = attributes.getRequest();
        Object userObj = request.getAttribute("loginUser");
        Users loginUser = (Users) userObj;
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未获取到登录用户信息");
        }
        //TODO 可以将登录用户信息缓存起来，下次请求时从缓存中获取
        long userId = loginUser.getUserId();
        loginUser = this.baseMapper.selectById(userId);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录用户不存在");
        }
        //发送获取登录用户日志
//        String action = ActionConstant.GET_LOGIN_USER;
//        sendMsg(action, userId);
        return loginUser;
    }
    //根据权限校验结果返回：<br>普通用户仅自己，管理员所有普通用户，超管全部


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
     * 获取脱敏类的用户信息和token
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

    /**
     * 获取用户信息
     *
     * @param user
     * @return
     */
    @Override
    public UserVO getUserVO(Users user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        userVO.setUserRole(permissionService.getUserRoleCode(user.getUserId()));
        return userVO;
    }

    /**
     * 是否为管理员
     *
     * @param
     * @return
     */
    @Override
    public boolean isAdmin() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请求上下文为空");
        }
        HttpServletRequest request = attributes.getRequest();
        Object userObj = request.getAttribute("loginUser");
        Users loginUser = (Users) userObj;
        return isAdmin(loginUser); // 判断是否为 admin 角色
    }

    /**
     * 是否为管理员
     *
     * @param
     * @return
     */
    @Override
    public boolean isAdmin(Users user) {
        return user != null
                && UserRoleEnum.ADMIN.getRoleCode()
                .equals(permissionService.getUserRoleCode(user.getUserId()));
    }

    /**
     * 是否为超级管理员
     *
     * @param
     * @return
     */
    @Override
    public boolean isSuperAdmin() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请求上下文为空");
        }
        HttpServletRequest request = attributes.getRequest();
        Object userObj = request.getAttribute("loginUser");
        Users loginUser = (Users) userObj;
        // 判断是否为 superadmin 角色
        return isSuperAdmin(loginUser);
    }

    /**
     * 是否为超级管理员
     *
     * @param
     * @return
     */
    @Override
    public boolean isSuperAdmin(Users user) {
        return user != null
                && UserRoleEnum.SUPER_ADMIN.getRoleCode()
                .equals(permissionService.getUserRoleCode(user.getUserId()));
    }

    /**
     * 分页获取用户列表（全部）
     *
     * @param userQueryRequest
     * @return
     */
    @Override
    public Page<UserVO> listUserAllByPage(UsersQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取当前登录用户
        Users currentUser = getLoginUser();
        QueryWrapper<Users> queryWrapper = this.getQueryWrapper(userQueryRequest);
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        Page<Users> usersPage = this.page(new Page<>(current, pageSize), queryWrapper);
        //发送获取用户列表日志
//        String action = ActionConstant.LIST_USER_ALL;
//        sendMsg(action, currentUser.getUserId());
        return (Page<UserVO>) usersPage.convert(users -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(users, userVO);
            userVO.setUserRole(permissionService.getUserRoleCode(users.getUserId()));
            return userVO;
        });

    }

    /**
     * 分页获取用户列表（分权限）
     *
     * @param userQueryRequest
     * @return
     */
    @Override
    public Page<UserVO> listUserByPageWithAuth(UsersQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null || userQueryRequest.getUserId()<=0, ErrorCode.PARAMS_ERROR);
        Users currentUser = getLoginUser(); // 获取当前登录用户
        String currentUserRole = permissionService.getUserRoleCode(currentUser.getUserId());
        QueryWrapper<Users> queryWrapper = this.getQueryWrapper(userQueryRequest);
        int current = userQueryRequest.getCurrent();
        int pageSize = userQueryRequest.getPageSize();
        // 防爬限制
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        if (UserRoleEnum.USER.getRoleCode().equals(currentUserRole)) {
            // 普通用户只能看自己
            queryWrapper.eq("user_id", currentUser.getUserId());
        } else if (UserRoleEnum.ADMIN.getRoleCode().equals(currentUserRole)) {
            // 管理员不能查看超级管理员（远程调用过滤）
            List<Long> superAdminIds = new ArrayList<>();
            // 小范围查询
            List<Users> usersList = this.list(queryWrapper);
            for (Users user : usersList) {
                if (UserRoleEnum.SUPER_ADMIN.getRoleCode()
                        .equals(permissionService.getUserRoleCode(user.getUserId()))) {
                    superAdminIds.add(user.getUserId());
                }
            }
            // 数据库层过滤
            if (!superAdminIds.isEmpty()) {
                queryWrapper.notIn("user_id", superAdminIds);
            }
        }
        // 超级管理员无限制
        Page<Users> usersPage = this.page(new Page<>(current, pageSize), queryWrapper);
        if (usersPage == null) {
            return new Page<>();
        }
        // 发送不同权限获取用户列表日志
//        String action = ActionConstant.LIST_USER_WITH_AUTH;
//        sendMsg(action, currentUser.getUserId());
        return (Page<UserVO>) usersPage.convert(users -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(users, userVO);
            userVO.setUserRole(permissionService.getUserRoleCode(users.getUserId()));
            return userVO;
        });
    }

    /**
     *
     * @param usersUpdateRequest
     * @return
     */
    @Override
    public boolean updateUser(UsersUpdateRequest usersUpdateRequest) {
        if (usersUpdateRequest == null || usersUpdateRequest.getUserId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long userId = usersUpdateRequest.getUserId();
        //判断是否存在
        Users oldUser = this.getById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        Users currentUser = getLoginUser();
        String currentUserRole = permissionService.getUserRoleCode(currentUser.getUserId());
        //根据权限限制：<br>普通用户改自己，管理员改普通用户和改自己，超管改所有
        if (!this.isAdmin() && !this.isSuperAdmin()) {
            if (!currentUser.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        } else if (UserRoleEnum.ADMIN.getRoleCode().equals(currentUserRole)) {
            // 管理员不能修改超级管理员
            String oldUserRole = permissionService.getUserRoleCode(userId);
            if (UserRoleEnum.SUPER_ADMIN.getRoleCode().equals(oldUserRole)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "管理员不能修改超级管理员");
            }
        }
        Users users = new Users();
        BeanUtils.copyProperties(usersUpdateRequest, users);
        // 发送日志
//        String action = ActionConstant.UPDATE_USER;
//        sendMsg(action, currentUser.getUserId());
        return true;
    }

    /**
     * 重置密码
     * @param userPasswordUpdateRequest
     * @return
     */
    @Override
    public boolean resetPassword(UserPasswordUpdateRequest userPasswordUpdateRequest) {
        if (userPasswordUpdateRequest == null || userPasswordUpdateRequest.getUserId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long userId = userPasswordUpdateRequest.getUserId();
        Users currentUser = getLoginUser();
        String currentUserRole = permissionService.getUserRoleCode(currentUser.getUserId());
        String oldPassword = userPasswordUpdateRequest.getOldPassword();
        String newPassword = userPasswordUpdateRequest.getNewPassword();
        String checkPassword = userPasswordUpdateRequest.getCheckPassword();
        // 校验新密码是否符合要求
        if (StringUtils.isAnyBlank(newPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能为空");
        }
        if (newPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能小于8位");
        }
        if (!newPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 权限判断
        if (!isAdmin() && !isSuperAdmin()) {
            if (!currentUser.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限修改他人密码");
            }
        } else if (UserRoleEnum.ADMIN.getRoleCode().equals(currentUserRole)) {
            String targetUserRole = permissionService.getUserRoleCode(userId);
            if (UserRoleEnum.SUPER_ADMIN.getRoleCode().equals(targetUserRole)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "管理员不能重置超级管理员密码");
            }
        }
        //判断是否存在
        Users oldUser = this.getById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 验证原密码
        String encryptedOldPassword = getEncryptPassword(oldPassword);
        if (!oldUser.getPassword().equals(encryptedOldPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "原密码错误");
        }
        // 加密新密码并更新
        String encryptedNewPassword = getEncryptPassword(newPassword);
        oldUser.setPassword(encryptedNewPassword);
        // 发送日志
//        String action = ActionConstant.RESET_PASSWORD;
//        sendMsg(action, currentUser.getUserId());
        return true;
    }

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Users> getQueryWrapper(UsersQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long userId = userQueryRequest.getUserId();
        String username = userQueryRequest.getUsername();
        String profile = userQueryRequest.getProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(userId != null, "user_id", userId);
        queryWrapper.like(StringUtils.isNotBlank(username), "username", username);
        queryWrapper.like(StringUtils.isNotBlank(profile), "profile", profile);
        queryWrapper.like(StringUtils.isNotBlank(userRole), "user_role", userRole);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }


    /**
     * 抽象方法，用于发送消息
     *
     * @param action
     * @param userId
     */
    private void sendMsg(String action, long userId, Object oldObj, Object newObj) {
        String ip = IpUtils.getIpAddress();

        // 获取字段变更详情
        Map<String, Object> fieldChanges = BeanDiffUtil.diff(oldObj, newObj);
        String logMessage = String.format(
                "{\"action\":\"%s\",\"userId\":%d,\"ip\":\"%s\",\"detail\":%s}",
                action, userId, ip, JSONUtil.toJsonStr(fieldChanges)
        );

        Message<String> streamMessage = MessageBuilder.withPayload(logMessage).build();
        streamBridge.send("handleMessage-out-0", streamMessage);
    }


}
