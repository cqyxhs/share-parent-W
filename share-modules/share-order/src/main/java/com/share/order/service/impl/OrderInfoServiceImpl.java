package com.share.order.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.share.common.core.constant.SecurityConstants;
import com.share.common.core.domain.R;
import com.share.common.security.utils.SecurityUtils;
import com.share.order.domain.*;
import com.share.order.mapper.OrderBillMapper;
import com.share.order.mapper.OrderInfoMapper;
import com.share.order.service.IOrderInfoService;
import com.share.rules.api.RemoteFeeRuleService;
import com.share.rules.domain.FeeRule;
import com.share.rules.domain.FeeRuleRequestForm;
import com.share.rules.domain.FeeRuleResponseVo;
import com.share.user.api.RemoteUserInfoService;
import com.share.user.domain.UserInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 订单Service业务层处理
 */
@Slf4j
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements IOrderInfoService {

    @Resource
    private RemoteFeeRuleService remoteFeeRuleService;

    @Resource
    private RemoteUserInfoService remoteUserInfoService;

    @Resource
    private OrderBillMapper orderBillMapper;

    @Override
    public OrderInfo getNoFinishOrder(Long userId) {
        // 查询用户是否有使用中与未支付订单
        return baseMapper.selectOne(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getUserId, userId)
                .in(OrderInfo::getStatus, Arrays.asList("0", "1"))// 订单状态：0:充电中 1：未支付 2：已支付
                .orderByDesc(OrderInfo::getId)
                .last("limit 1")
        );
    }

    //获取订单详细信息
    @Override
    public OrderInfo selectOrderInfoById(Long id) {
        //根据id查询订单信息
        OrderInfo orderInfo = baseMapper.selectById(id);

        //判断订单状态充电中 0
        if("0".equals(orderInfo.getStatus())) {
            //计算充电时间
            int minutes = Minutes.minutesBetween(new DateTime(orderInfo.getStartTime()),
                    new DateTime()).getMinutes();
            //充电时间大于0
            if(minutes > 0) {
                orderInfo.setDuration(minutes);

                FeeRuleRequestForm feeRuleRequestForm = new FeeRuleRequestForm();
                feeRuleRequestForm.setDuration(minutes);
                feeRuleRequestForm.setFeeRuleId(orderInfo.getFeeRuleId());
                R<FeeRuleResponseVo> feeRuleResponseVoR =
                        remoteFeeRuleService.calculateOrderFee(feeRuleRequestForm, SecurityConstants.INNER);
                FeeRuleResponseVo responseVo = feeRuleResponseVoR.getData();

                //设置到orderInfo里面
                orderInfo.setTotalAmount(responseVo.getTotalAmount());
                orderInfo.setDeductAmount(new BigDecimal(0));
                orderInfo.setRealAmount(responseVo.getTotalAmount());
            } else {
                orderInfo.setDuration(0);
                orderInfo.setTotalAmount(new BigDecimal(0));
                orderInfo.setDeductAmount(new BigDecimal(0));
                orderInfo.setRealAmount(new BigDecimal(0));
            }
        }

        //OrderBill
        List<OrderBill> orderBillList = orderBillMapper.selectList(new LambdaQueryWrapper<OrderBill>().eq(OrderBill::getOrderId, id));
        orderInfo.setOrderBillList(orderBillList);

        R<UserInfo> userInfoR = remoteUserInfoService.getInfo(orderInfo.getUserId(),SecurityConstants.INNER);
        UserInfo userInfo = userInfoR.getData();
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo,userInfoVo);
        orderInfo.setUserInfoVo(userInfoVo);

        //返回对象
        return orderInfo;
    }

    //结束订单
    @Override
    public void endOrder(EndOrderVo endOrderVo) {
        //1 根据充电宝编号 + 订单状态（充电中）查询是否存在，如果不存在订单，直接返回
        //如果存在正在充电的订单，把订单结束
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getPowerBankNo,endOrderVo.getPowerBankNo());
        wrapper.eq(OrderInfo::getStatus,"0");
        OrderInfo orderInfo = baseMapper.selectOne(wrapper);
        //判断
        if(orderInfo == null) { //如果不存在订单，直接返回
            return;
        }

        //2 设置订单相关数据，进行更新
        orderInfo.setEndTime(endOrderVo.getEndTime());
        orderInfo.setEndStationId(endOrderVo.getEndStationId());
        orderInfo.setEndStationName(endOrderVo.getEndStationName());
        orderInfo.setEndCabinetNo(endOrderVo.getEndCabinetNo());
        // 包含充电时长 以分钟单位
        Date startTime = orderInfo.getStartTime();
        Date endTime = orderInfo.getEndTime();
        int duration = Minutes.minutesBetween(new DateTime(startTime),
                new DateTime(endTime)).getMinutes();
        orderInfo.setDuration(duration);

        //远程调用：规则引擎进行费用计算
        //封装参数
        FeeRuleRequestForm feeRuleRequestForm = new FeeRuleRequestForm();
        feeRuleRequestForm.setDuration(duration);
        feeRuleRequestForm.setFeeRuleId(orderInfo.getFeeRuleId());
        //远程调用
        R<FeeRuleResponseVo> feeRuleResponseVoR = remoteFeeRuleService.calculateOrderFee(feeRuleRequestForm,SecurityConstants.INNER);
        FeeRuleResponseVo feeRuleResponseVo = feeRuleResponseVoR.getData();

        //设置费用
        orderInfo.setTotalAmount(feeRuleResponseVo.getTotalAmount());
        orderInfo.setDeductAmount(new BigDecimal("0"));
        orderInfo.setRealAmount(feeRuleResponseVo.getTotalAmount());

        if(orderInfo.getRealAmount().subtract(new BigDecimal(0)).doubleValue() == 0) {
            orderInfo.setStatus("2");
        } else {
            orderInfo.setStatus("1");
        }
        baseMapper.updateById(orderInfo);

        //3 插入免费账单数据
        OrderBill freeOrderBill = new OrderBill();
        freeOrderBill.setOrderId(orderInfo.getId());
        freeOrderBill.setBillItem(feeRuleResponseVo.getFreeDescription());
        freeOrderBill.setBillAmount(new BigDecimal(0));
        orderBillMapper.insert(freeOrderBill);

        //4 插入收费账单数据（超过免费时间账单数据）
        BigDecimal exceedPrice = feeRuleResponseVo.getExceedPrice();
        if(exceedPrice.doubleValue()>0) {
            OrderBill exceedOrderBill = new OrderBill();
            exceedOrderBill.setOrderId(orderInfo.getId());
            exceedOrderBill.setBillItem(feeRuleResponseVo.getExceedDescription());
            exceedOrderBill.setBillAmount(feeRuleResponseVo.getExceedPrice());
            orderBillMapper.insert(exceedOrderBill);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long saveOrder(SubmitOrderVo orderForm) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setUserId(orderForm.getUserId());
        orderInfo.setOrderNo(RandomUtil.randomString(8));
        orderInfo.setPowerBankNo(orderForm.getPowerBankNo());
        orderInfo.setStartTime(new Date());
        orderInfo.setStartStationId(orderForm.getStartStationId());
        orderInfo.setStartStationName(orderForm.getStartStationName());
        orderInfo.setStartCabinetNo(orderForm.getStartCabinetNo());
        // 费用规则
        orderInfo.setFeeRuleId(orderForm.getFeeRuleId());
        R<FeeRule> feeRuleResult = remoteFeeRuleService.getFeeRule(orderForm.getFeeRuleId(), SecurityConstants.INNER);
        if (feeRuleResult != null && feeRuleResult.getData() != null) {
            orderInfo.setFeeRule(feeRuleResult.getData().getDescription());
        } else {
            orderInfo.setFeeRule("");
            log.warn("获取费用规则失败，feeRuleId: {}", orderForm.getFeeRuleId());
        }
        orderInfo.setStatus("0");
        orderInfo.setCreateTime(new Date());
        orderInfo.setCreateBy(SecurityUtils.getUsername());

        baseMapper.insert(orderInfo);
        return orderInfo.getId();
    }

    //修改订单状态
    @Override
    public void processPaySucess(String orderNo) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getOrderNo,orderNo);
        OrderInfo orderInfo = baseMapper.selectOne(wrapper);

        if("1".equals(orderInfo.getStatus())) {
            orderInfo.setStatus("2");
            orderInfo.setPayTime(new Date());
            baseMapper.updateById(orderInfo);
        }
    }

    @Override
    public Map<String, Object> getOrderCount(String sql) {
        List<Map<String, Object>> list = baseMapper.getOrderCount(sql);

        Map dataMap = new HashMap<>();

        List<Object> monthList = new ArrayList<>();
        List<Object> orderCountList = new ArrayList<>();

        for (Map<String, Object> map : list) {
            monthList.add(map.get("order_date"));
            orderCountList.add(map.get("order_count"));
        }

        dataMap.put("dateList", monthList);
        dataMap.put("countList", orderCountList);

        return dataMap;
    }
}
