package com.share.rules.api;

import com.share.common.core.domain.R;
import com.share.common.security.annotation.InnerAuth;
import com.share.rules.domain.FeeRule;
import com.share.rules.domain.FeeRuleRequestForm;
import com.share.rules.domain.FeeRuleResponseVo;
import com.share.rules.service.IFeeRuleService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/feeRule")
public class FeeRuleApiController {

    @Resource
    private IFeeRuleService feeRuleService;

    @Operation(summary = "批量获取费用规则信息")
    @InnerAuth
    @PostMapping(value = "/getFeeRuleList")
    public R<List<FeeRule>> getFeeRuleList(@RequestBody List<Long> feeRuleIdList)
    {
        return R.ok(feeRuleService.listByIds(feeRuleIdList));
    }

    //根据id获取规则详情
    @Operation(summary = "获取费用规则详细信息")
    @InnerAuth
    @GetMapping(value = "/getFeeRule/{id}")
    public R<FeeRule> getFeeRule(@PathVariable("id") Long id)
    {
        return R.ok(feeRuleService.getById(id));
    }

    @Operation(summary = "计算订单费用")
    @InnerAuth
    @PostMapping("/calculateOrderFee")
    public R<FeeRuleResponseVo> calculateOrderFee(@RequestBody FeeRuleRequestForm calculateOrderFeeForm) {
        return R.ok(feeRuleService.calculateOrderFee(calculateOrderFeeForm));
    }
}