package com.share.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.share.user.domain.UserLoginLog;
import com.share.user.mapper.UserLoginLogMapper;
import com.share.user.service.IUserLoginLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户登录记录Service业务层处理
 */
@Service
public class UserLoginLogServiceImpl extends ServiceImpl<UserLoginLogMapper, UserLoginLog> implements IUserLoginLogService
{
    @Resource
    private UserLoginLogMapper userLoginLogMapper;

    /**
     * 查询用户登录记录列表
     *
     * @param userLoginLog 用户登录记录
     * @return 用户登录记录
     */
    @Override
    public List<UserLoginLog> selectUserLoginLogList(UserLoginLog userLoginLog)
    {
        return userLoginLogMapper.selectUserLoginLogList(userLoginLog);
    }

}
