package com.share.device.emqx.handler.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.share.common.core.utils.StringUtils;
import com.share.device.domain.Cabinet;
import com.share.device.domain.CabinetSlot;
import com.share.device.domain.PowerBank;
import com.share.device.domain.Station;
import com.share.device.emqx.annotation.GuiguEmqx;
import com.share.device.emqx.constant.EmqxConstants;
import com.share.device.emqx.handler.MassageHandler;
import com.share.device.service.ICabinetService;
import com.share.device.service.ICabinetSlotService;
import com.share.device.service.IPowerBankService;
import com.share.device.service.IStationService;
import com.share.order.domain.SubmitOrderVo;
import com.share.common.rabbit.constant.MqConst;
import com.share.common.rabbit.service.RabbitService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@GuiguEmqx(topic = EmqxConstants.TOPIC_POWERBANK_UNLOCK)
public class PowerBankUnlockHandler implements MassageHandler {
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private ICabinetService cabinetService;

    @Resource
    private ICabinetSlotService cabinetSlotService;

    @Resource
    private IPowerBankService powerBankService;

    @Resource
    private IStationService stationService;
    @Resource
    private RabbitService rabbitService;

    /*
     * 充电宝弹出后续处理
     * 处理消息:
     * @param message
     * {"messageNo":"7777","slotNo":"1","userId":1,
     * "cabinetNo":"xgxgxxxg","powerBankNo":"gg001"}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleMessage(JSONObject message) {
        log.info("handleMessage: {}", message.toJSONString());

        //1 获取messageNo，基于redis防止重复提交
        String messageNo = message.getString("mNo");

        String key = "powerBank:unlock:"+messageNo;
        Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent(key, messageNo, 1, TimeUnit.HOURS);
        // 防止重复提交 setnx方法,false重复提交，return.
        if(Boolean.FALSE.equals(ifAbsent)) {
            return;
        }


        // 2 获取柜机编号CabinetNo、充电宝编号PowerBankNo、插槽编号SlotNo
            // 用户id：userId，非空判断
            //柜机编号
        String cabinetNo = message.getString("cNo");
            //充电宝编号
        String powerBankNo = message.getString("pNo");
            //插槽编号
        String slotNo = message.getString("sNo");
            //用户id
        Long userId = message.getLong("userId");
        if (StringUtils.isEmpty(cabinetNo) || StringUtils.isEmpty(powerBankNo) || StringUtils.isEmpty(slotNo) || null == userId) {
            log.info("参数为空: {}", message.toJSONString());
            return;
        }

        // 3 根据柜机编号获取柜机信息
        Cabinet cabinet = cabinetService.getBtCabinetNo(cabinetNo);

        // 4 获取充电宝信息根据充电宝编号
        PowerBank powerBank = powerBankService.getByPowerBankNo(powerBankNo);

        // 5 根据柜机id+插槽编号获取插槽信息
        CabinetSlot cabinetSlot = cabinetSlotService.getBtSlotNo(cabinet.getId(), slotNo);

        // 6 根据柜机id获取站点数据
        LambdaQueryWrapper<Station> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Station::getCabinetId,cabinet.getId());
        Station station = stationService.getOne(wrapper);

        //7 更新充电宝状态
        powerBank.setStatus("2"); //已租用
        powerBankService.updateById(powerBank);

        //8 更新插槽状态
        cabinetSlot.setStatus("0");
        cabinetSlot.setPowerBankId(null);
        cabinetSlot.setUpdateTime(new Date());
        cabinetSlotService.updateById(cabinetSlot);

        //9 更新柜机状态
        int freeSlots = cabinet.getFreeSlots() + 1;
        cabinet.setFreeSlots(freeSlots);
        int usedSlots = cabinet.getUsedSlots() - 1;
        cabinet.setUsedSlots(usedSlots);
        //可以借用
        int availableNum = cabinet.getAvailableNum() - 1;
        cabinet.setAvailableNum(availableNum);
        cabinet.setUpdateTime(new Date());
        cabinetService.updateById(cabinet);

        // 10 使用RabbitMQ异步生成订单
        //封装消息
        SubmitOrderVo submitOrderVo = new SubmitOrderVo();
        submitOrderVo.setMessageNo(messageNo);
        submitOrderVo.setUserId(userId);
        submitOrderVo.setPowerBankNo(powerBankNo);
        submitOrderVo.setStartStationId(station.getId());
        submitOrderVo.setStartStationName(station.getName());
        submitOrderVo.setStartCabinetNo(cabinetNo);
        submitOrderVo.setFeeRuleId(station.getFeeRuleId());
        //发送mq消息
        rabbitService.sendMessage(MqConst.EXCHANGE_ORDER,
                MqConst.ROUTING_SUBMIT_ORDER,
                com.alibaba.fastjson2.JSONObject.toJSONString(submitOrderVo));

    }
}