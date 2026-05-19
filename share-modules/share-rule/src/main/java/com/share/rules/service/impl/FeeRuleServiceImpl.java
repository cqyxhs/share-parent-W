package com.share.rules.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.share.rules.config.DroolsHelper;
import com.share.rules.domain.FeeRule;
import com.share.rules.domain.FeeRuleRequestForm;
import com.share.rules.domain.FeeRuleResponseVo;
import com.share.rules.domain.vo.FeeRuleRequest;
import com.share.rules.domain.vo.FeeRuleResponse;
import com.share.rules.mapper.FeeRuleMapper;
import com.share.rules.service.IFeeRuleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class FeeRuleServiceImpl extends ServiceImpl<FeeRuleMapper, FeeRule> implements IFeeRuleService
{
    @Resource
    private FeeRuleMapper feeRuleMapper;
    @Resource
    private KieContainer kieContainer;


    @Override
    public List<FeeRule> selectFeeRuleList(FeeRule feeRule)
    {
        return feeRuleMapper.selectFeeRuleList(feeRule);
    }

    @Override
    public List<FeeRule> getALLFeeRuleList() {
        return feeRuleMapper.selectList(new LambdaQueryWrapper<FeeRule>().eq(FeeRule::getStatus, "1"));
    }

    @Override
    public FeeRuleResponseVo calculateOrderFee(FeeRuleRequestForm feeRuleRequestForm) {

        //1 封装传入对象
        FeeRuleRequest feeRuleRequest = new FeeRuleRequest();
        feeRuleRequest.setDurations(feeRuleRequestForm.getDuration());
        log.info("传入参数：{}", JSON.toJSONString(feeRuleRequest));


        //2 获取最新订单费用规则
        FeeRule feeRule = feeRuleMapper.selectById(feeRuleRequestForm.getFeeRuleId());
        KieSession kieSession = DroolsHelper.loadForRule(feeRule.getRule());

        //4 封装返回对象
        FeeRuleResponse feeRuleResponse = new FeeRuleResponse();
        kieSession.setGlobal("feeRuleResponse", feeRuleResponse);

        // 设置订单对象
        kieSession.insert(feeRuleRequest);
        // 触发规则
        kieSession.fireAllRules();
        // 中止会话
        kieSession.dispose();

        log.info("计算结果：{}", JSON.toJSONString(feeRuleResponse));

        //封装返回对象
        FeeRuleResponseVo feeRuleResponseVo = new FeeRuleResponseVo();
        feeRuleResponseVo.setTotalAmount(BigDecimal.valueOf(feeRuleResponse.getTotalAmount()));
        feeRuleResponseVo.setFreePrice(BigDecimal.valueOf(feeRuleResponse.getFreePrice()));
        feeRuleResponseVo.setExceedPrice(BigDecimal.valueOf(feeRuleResponse.getExceedPrice()));
        feeRuleResponseVo.setFreeDescription(feeRuleResponse.getFreeDescription());
        feeRuleResponseVo.setExceedDescription(feeRuleResponse.getExceedDescription());

        return feeRuleResponseVo;
    }
}
