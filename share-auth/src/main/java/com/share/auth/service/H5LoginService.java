package com.share.auth.service;

import com.share.common.core.constant.Constants;
import com.share.common.core.constant.SecurityConstants;
import com.share.common.core.domain.R;
import com.share.common.core.exception.ServiceException;
import com.share.common.core.utils.StringUtils;
import com.share.common.core.utils.ip.IpUtils;
import com.share.system.api.model.LoginUser;
import com.share.user.api.RemoteUserInfoService;
import com.share.user.domain.UpdateUserLogin;
import com.share.user.domain.UserInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
public class H5LoginService
{
    @Resource
    private RemoteUserInfoService remoteUserInfoService;

    @Resource
    private SysRecordLogService recordLogService;

    /**
     * 登录
     */
    public LoginUser login(String code)
    {
        // 1 判断code是否为空
        if (StringUtils.isEmpty(code))
        {
            throw new ServiceException("微信code必须填写");
        }
        // 2 使用code进行远程调用，返回userInfo
        R<UserInfo> userResult = remoteUserInfoService.wxLogin(code,SecurityConstants.INNER);
        System.out.println("【小程序授权】userResult.getData="+userResult.getData());
        System.out.println("【小程序授权】userResult.getCode="+userResult.getCode());
        UserInfo userInfo = userResult.getData();
        // 3 判断是否登录成功
        if (userInfo == null)
        {
            throw new ServiceException("微信授权登录失败");
        }

        // 4 返回数据封装
        LoginUser loginUser = new LoginUser();
        loginUser.setUserid(userInfo.getId());
        loginUser.setUsername(userInfo.getWxOpenId());
        loginUser.setStatus(userInfo.getStatus());

        if (userInfo.getStatus().equals("2"))
        {
            recordLogService.recordLogininfor(userInfo.getWxOpenId(), Constants.LOGIN_FAIL, "用户已停用，请联系管理员");
            throw new ServiceException("对不起，您的账号：" + userInfo.getWxOpenId() + " 已停用");
        }
        recordLogService.recordLogininfor(userInfo.getWxOpenId(), Constants.LOGIN_SUCCESS, "登录成功");

        //更新登录信息
        UpdateUserLogin updateUserLogin = new UpdateUserLogin();
        updateUserLogin.setUserId(userInfo.getId());
        updateUserLogin.setLastLoginIp(IpUtils.getIpAddr());
        updateUserLogin.setLastLoginTime(new Date());
        remoteUserInfoService.updateUserLogin(updateUserLogin,SecurityConstants.INNER);

        // 5 返回登录信息
        return loginUser;
    }
}
