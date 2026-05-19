package com.share.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.share.user.domain.UserLoginLog;

import java.util.List;

/**
 * 用户登录记录Mapper接口
 */
public interface UserLoginLogMapper extends BaseMapper<UserLoginLog>
{

    /**
     * 查询用户登录记录列表
     *
     * @param userLoginLog 用户登录记录
     * @return 用户登录记录集合
     */
    List<UserLoginLog> selectUserLoginLogList(UserLoginLog userLoginLog);

}
