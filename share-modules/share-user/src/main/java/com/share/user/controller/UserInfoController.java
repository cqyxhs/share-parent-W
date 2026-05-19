package com.share.user.controller;

import com.share.common.core.domain.R;
import com.share.common.core.utils.poi.ExcelUtil;
import com.share.common.security.utils.SecurityUtils;
import com.share.system.api.model.LoginUser;
import com.share.common.core.web.controller.BaseController;
import com.share.common.core.web.domain.AjaxResult;
import com.share.common.core.web.page.TableDataInfo;
import com.share.common.log.annotation.Log;
import com.share.common.log.enums.BusinessType;
import com.share.common.security.annotation.RequiresPermissions;
import com.share.user.domain.UpdateUserLogin;
import com.share.user.domain.UserInfo;
import com.share.user.service.IUserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 用户Controller
 */
@Tag(name = "用户接口管理")
@RestController
@RequestMapping("/userInfo")
public class UserInfoController extends BaseController
{
    @Resource
    private IUserInfoService userInfoService;

    /**
     * 查询用户列表
     */
    @Operation(summary = "查询用户列表")
    @RequiresPermissions("user:userInfo:list")
    @GetMapping("/list")
    public TableDataInfo list(UserInfo userInfo)
    {
        startPage();
        List<UserInfo> list = userInfoService.selectUserInfoList(userInfo);
        return getDataTable(list);
    }

    /**
     * 导出用户列表
     */
    @Operation(summary = "导出用户列表")
    @RequiresPermissions("user:userInfo:export")
    @Log(title = "用户", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UserInfo userInfo)
    {
        List<UserInfo> list = userInfoService.selectUserInfoList(userInfo);
        ExcelUtil<UserInfo> util = new ExcelUtil<>(UserInfo.class);
        util.exportExcel(response, list, "用户数据");
    }

    /**
     * 获取用户详细信息
     */
    @Operation(summary = "获取用户详细信息")
    @RequiresPermissions("user:userInfo:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(userInfoService.getById(id));
    }

    /**
     * 根据微信OpenID获取用户信息
     */
    @Operation(summary = "根据微信OpenID获取用户信息")
    @GetMapping("/getLoginUserInfo")
    public R<UserInfo> getLoginUserInfo()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null)
        {
            return R.fail("用户未登录");
        }
        String wxOpenId = loginUser.getUsername();
        UserInfo userInfo = userInfoService.lambdaQuery().eq(UserInfo::getWxOpenId, wxOpenId).one();
        return R.ok(userInfo);
    }

    /**
     * 新增用户
     */
    @Operation(summary = "新增用户")
    @RequiresPermissions("user:userInfo:add")
    @Log(title = "用户", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody UserInfo userInfo)
    {
        return toAjax(userInfoService.save(userInfo));
    }

    /**
     * 修改用户
     */
    @Operation(summary = "修改用户")
    @RequiresPermissions("user:userInfo:edit")
    @Log(title = "用户", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody UserInfo userInfo)
    {
        return toAjax(userInfoService.updateById(userInfo));
    }

    /**
     * 更新用户登录信息
     */
    @Operation(summary = "更新用户登录信息")
    @PutMapping("/updateUserLogin")
    public R<Boolean> updateUserLogin(@RequestBody UpdateUserLogin updateUserLogin)
    {
        return R.ok(userInfoService.updateUserLogin(updateUserLogin));
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户")
    @RequiresPermissions("user:userInfo:remove")
    @Log(title = "用户", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(userInfoService.removeBatchByIds(Arrays.asList(ids)));
    }


}
