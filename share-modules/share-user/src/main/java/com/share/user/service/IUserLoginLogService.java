package com.share.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.share.user.domain.UserLoginLog;

import java.util.List;

/**
 * 用户登录记录Service接口
 */
public interface IUserLoginLogService extends IService<UserLoginLog>
{

    /**
     * 查询用户登录记录列表
     *
     * @param userLoginLog 用户登录记录
     * @return 用户登录记录集合
     */
    public List<UserLoginLog> selectUserLoginLogList(UserLoginLog userLoginLog);

}
