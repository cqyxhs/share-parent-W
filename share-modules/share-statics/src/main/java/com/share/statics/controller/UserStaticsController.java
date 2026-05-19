package com.share.statics.controller;

import com.share.common.core.constant.SecurityConstants;
import com.share.common.core.domain.R;
import com.share.common.core.web.controller.BaseController;
import com.share.common.core.web.domain.AjaxResult;
import com.share.user.api.RemoteUserInfoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "AI数据统计")
@RestController
@RequestMapping("/statics")
public class UserStaticsController extends BaseController {

    @Resource
    private RemoteUserInfoService remoteUserInfoService;

    //统计某个年段注册的用户数量
    @GetMapping("/userCount/{startYear}/{endYear}")
    public AjaxResult userCount(@PathVariable("startYear") Integer startYear, @PathVariable("endYear") Integer endYear) {
        R<Map<String,Object>> result = remoteUserInfoService.getUserCount(startYear,endYear,SecurityConstants.INNER);
        System.out.println(result);
        Map<String, Object> map = result.getData();
        return success(map);
    }
}
