package com.share.user.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.share.common.core.context.SecurityContextHolder;
import com.share.common.core.exception.ServiceException;
import com.share.user.domain.UpdateUserLogin;
import com.share.user.domain.UserCountVo;
import com.share.user.domain.UserInfo;
import com.share.user.domain.UserLoginLog;
import com.share.user.mapper.UserInfoMapper;
import com.share.user.mapper.UserLoginLogMapper;
import com.share.user.service.IUserInfoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户Service业务层处理
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService
{
    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private WxMaService wxMaService;

    @Resource
    private UserLoginLogMapper userLoginLogMapper;

    /**
     * 查询用户列表
     *
     * @param userInfo 用户
     * @return 用户
     */
    @Override
    public List<UserInfo> selectUserInfoList(UserInfo userInfo)
    {
        return userInfoMapper.selectUserInfoList(userInfo);
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public UserInfo wxLogin(String code) {
        String openId;
        try {
            //1 使用code+appid+secret获取openId
            WxMaJscode2SessionResult sessionInfo = wxMaService.getUserService().getSessionInfo(code);
            openId = sessionInfo.getOpenid();
            System.out.println("【小程序授权】openId={}"+openId);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ServiceException("微信登录失败");
        }
        // 2 根据openId查询数据库是否有该用户
        UserInfo userInfo = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getWxOpenId, openId));
        // 3 如果没有该用户，就注册一个新用户
        if (userInfo ==null ) {
            userInfo = new UserInfo();
            userInfo.setNickname(String.valueOf(System.currentTimeMillis()));
            userInfo.setAvatarUrl("https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
            userInfo.setWxOpenId(openId);
            userInfo.setStatus("1");
            userInfoMapper.insert(userInfo);
        }
        // 4 返回该用户信息
        return userInfo;
    }

    @Override
    public Boolean updateUserLogin(UpdateUserLogin updateUserLogin) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(updateUserLogin.getUserId());
        userInfo.setLastLoginIp(updateUserLogin.getLastLoginIp());
        userInfo.setLastLoginTime(updateUserLogin.getLastLoginTime());
        userInfoMapper.updateById(userInfo);

        //登录日志
        UserLoginLog userLoginLog = new UserLoginLog();
        userLoginLog.setUserId(userInfo.getId());
        userLoginLog.setMsg("小程序登录");
        userLoginLog.setIpaddr(updateUserLogin.getLastLoginIp());
        userLoginLogMapper.insert(userLoginLog);
        return true;
    }

    @Override
    public Boolean isFreeDeposit() {
        //微信支付分
        //https://pay.weixin.qq.com/wiki/doc/apiv3/payscore.php?chapter=18_1&index=2
        // 默认免押金，模拟实现
        UserInfo userInfo = this.getById(SecurityContextHolder.getUserId());
        userInfo.setDepositStatus("1");
        this.updateById(userInfo);
        return true;
    }


    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserCountBetween(Integer startYear, Integer endYear) {
        List<UserCountVo> list = baseMapper.selectUserCountBetween(startYear, endYear);

        Map<String, Object> map = new HashMap<>();
        //日期列表
        List<String> dateList
                =list.stream().map(UserCountVo::getRegisterDate).collect(Collectors.toList());
        //统计列表
        List<Integer> countList
                =list.stream().map(UserCountVo::getCount).collect(Collectors.toList());
        map.put("dateList", dateList);
        map.put("countList", countList);
        return map;
    }
}
