package com.share.rules.api;

import com.share.common.core.constant.SecurityConstants;
import com.share.common.core.constant.ServiceNameConstants;
import com.share.common.core.domain.R;
import com.share.rules.domain.FeeRule;
import com.share.rules.domain.FeeRuleRequestForm;
import com.share.rules.domain.FeeRuleResponseVo;
import com.share.rules.factory.RemoteFeeRuleFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(contextId = "remoteFeeRuleService",
        value = ServiceNameConstants.RULE_SERVICE,
        fallbackFactory = RemoteFeeRuleFallbackFactory.class)
public interface RemoteFeeRuleService {

    @PostMapping(value = "/api/feeRule/getFeeRuleList")
    R<List<FeeRule>> getFeeRuleList(@RequestBody List<Long> feeRuleIdList,@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping(value = "/api/feeRule/getFeeRule/{id}")
    R<FeeRule> getFeeRule(@PathVariable("id") Long id,@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @PostMapping("/api/feeRule/calculateOrderFee")
    R<FeeRuleResponseVo> calculateOrderFee(@RequestBody FeeRuleRequestForm feeRuleRequestForm,@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

}