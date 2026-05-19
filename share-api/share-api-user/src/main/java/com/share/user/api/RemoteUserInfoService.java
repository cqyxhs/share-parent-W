package com.share.user.api;

import com.share.common.core.constant.SecurityConstants;
import com.share.common.core.constant.ServiceNameConstants;
import com.share.common.core.domain.R;
import com.share.user.domain.UpdateUserLogin;
import com.share.user.domain.UserInfo;
import com.share.user.factory.RemoteUserFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户服务
 */
@FeignClient(contextId = "remoteUserInfoService", value = ServiceNameConstants.USER_SERIVCE, fallbackFactory = RemoteUserFallbackFactory.class)
@Service
public interface RemoteUserInfoService
{
    @GetMapping("/userInfo/wxLogin/{code}")
    R<UserInfo> wxLogin(@PathVariable("code") String code,@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @PutMapping("/userInfo/updateUserLogin")
    R<Boolean> updateUserLogin(@RequestBody UpdateUserLogin updateUserLogin,@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping(value = "/userInfo/getUserInfo/{id}")
    R<UserInfo> getInfo(@PathVariable("id") Long id,@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping(value = "/userInfo/getUserCount/{startYear}/{endYear}")
    R<Map<String, Object>> getUserCount(@PathVariable("startYear") Integer startYear, @PathVariable("endYear") Integer endYear,@RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
