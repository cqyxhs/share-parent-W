package com.share.user.api;

import com.share.common.core.context.SecurityContextHolder;
import com.share.common.core.domain.R;
import com.share.common.core.utils.bean.BeanUtils;
import com.share.common.core.web.controller.BaseController;
import com.share.common.core.web.domain.AjaxResult;
import com.share.common.core.web.page.TableDataInfo;
import com.share.common.security.annotation.InnerAuth;
import com.share.common.security.annotation.RequiresLogin;
import com.share.user.domain.UpdateUserLogin;
import com.share.user.domain.UserInfo;
import com.share.user.domain.UserVo;
import com.share.user.service.IUserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/userInfo")
public class UserInfoApiController extends BaseController {

    @Resource
    private IUserInfoService userInfoService;

    //微信授权登录-远程调用
    @Operation(summary = "小程序授权登录")
    @InnerAuth
    @GetMapping("/wxLogin/{code}")
    public R<UserInfo> wxLogin(@PathVariable  String code){
        return R.ok(userInfoService.wxLogin(code));
    }



    @Operation(summary = "是否免押金")
    @RequiresLogin
    @GetMapping("/isFreeDeposit")
    public AjaxResult isFreeDeposit() {
        return success(userInfoService.isFreeDeposit());
    }

    @Operation(summary = "获取用户详细信息")
    @GetMapping(value = "/getUserInfo/{id}")
    public R<UserInfo> getInfo(@PathVariable("id") Long id) {
        UserInfo userInfo = userInfoService.getById(id);
        return R.ok(userInfo);
    }


    @Operation(summary = "获取指定年份区间每月用户注册数量")
    @InnerAuth
    @GetMapping(value = "/getUserCount/{startYear}/{endYear}")
    public R<Map<String, Object>> getUserCountBetween(@PathVariable("startYear") Integer startYear, @PathVariable("endYear") Integer endYear) {
        Map<String, Object> map = userInfoService.getUserCountBetween(startYear, endYear);
        return R.ok(map);
    }
}