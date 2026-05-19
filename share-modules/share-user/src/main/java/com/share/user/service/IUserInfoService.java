package com.share.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.share.user.domain.UpdateUserLogin;
import com.share.user.domain.UserInfo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 用户Service接口

 */
@Service
public interface IUserInfoService extends IService<UserInfo>
{

    /**
     * 查询用户列表
     *
     * @param userInfo 用户
     * @return 用户集合
     */
    List<UserInfo> selectUserInfoList(UserInfo userInfo);

    UserInfo wxLogin(String code);

    Boolean updateUserLogin(UpdateUserLogin updateUserLogin);

    //扫码后判断是否免押金
    Boolean isFreeDeposit();

    /**
     * 获取指定年份区间每月用户注册数量
     * @param startYear 开始年份
     * @param endYear 结束年份
     * @return 用户注册统计数据
     */
    Map<String, Object> getUserCountBetween(Integer startYear, Integer endYear);
}
