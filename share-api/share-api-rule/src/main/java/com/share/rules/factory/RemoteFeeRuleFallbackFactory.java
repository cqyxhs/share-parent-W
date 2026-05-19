package com.share.rules.factory;


import com.share.common.core.domain.R;
import com.share.rules.api.RemoteFeeRuleService;
import com.share.rules.domain.FeeRule;
import com.share.rules.domain.FeeRuleRequestForm;
import com.share.rules.domain.FeeRuleResponseVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户服务降级处理
 */
@Component
public class RemoteFeeRuleFallbackFactory implements FallbackFactory<RemoteFeeRuleService> {
    private static final Logger log = LoggerFactory.getLogger(RemoteFeeRuleFallbackFactory.class);

    @Override
    public RemoteFeeRuleService create(Throwable throwable) {
        log.error("规则服务调用失败:{}", throwable.getMessage());
        return new RemoteFeeRuleService() {

            @Override
            public R<List<FeeRule>> getFeeRuleList(List<Long> feeRuleIdList, String source) {
                return R.fail("获取费用规则列表失败:" + throwable.getMessage());
            }

            @Override
            public R<FeeRule> getFeeRule(Long id, String source) {
                return R.fail("获取费用规则信息失败:" + throwable.getMessage());
            }

            @Override
            public R<FeeRuleResponseVo> calculateOrderFee(FeeRuleRequestForm feeRuleRequestForm, String source) {
                return R.fail("计算费用失败:" + throwable.getMessage());
            }
        };
    }
}