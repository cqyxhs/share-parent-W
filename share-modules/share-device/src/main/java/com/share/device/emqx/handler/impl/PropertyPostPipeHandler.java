package com.share.device.emqx.handler.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.share.common.core.constant.DeviceConstants;
import com.share.common.core.utils.StringUtils;
import com.share.device.domain.Cabinet;
import com.share.device.domain.CabinetSlot;
import com.share.device.domain.PowerBank;
import com.share.device.emqx.annotation.GuiguEmqx;
import com.share.device.emqx.constant.EmqxConstants;
import com.share.device.emqx.handler.MassageHandler;
import com.share.device.service.ICabinetService;
import com.share.device.service.ICabinetSlotService;
import com.share.device.service.IPowerBankService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@GuiguEmqx(topic = EmqxConstants.TOPIC_PROPERTY_POST)
public class PropertyPostPipeHandler implements MassageHandler {

    @Resource
    private ICabinetService cabinetService;

    @Resource
    private IPowerBankService powerBankService;

    @Resource
    private ICabinetSlotService cabinetSlotService;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 处理消息:
     * @param message 消息内容
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void handleMessage(JSONObject message) {
        String content = message.getString("content");
        log.info("handleMessage: {}", content);


        // 解析竖线分隔的消息 tmsg = "mNo="+mNo+"|cNo=gj001|pNo=cdb001|sNo=1|ety=90";
        JSONObject parsedMessage = new JSONObject();
        String[] pairs = content.split("\\|");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = keyValue[1];
                // 转换key的格式
                switch (key) {
                    case "messageNo":
                        parsedMessage.put("mNo", value);
                        break;
                    case "cabinetNo":
                        parsedMessage.put("cNo", value);
                        break;
                    case "powerBankNo":
                        parsedMessage.put("pNo", value);
                        break;
                    case "slotNo":
                        parsedMessage.put("sNo", value);
                        break;
                    case "electricity":
                        parsedMessage.put("electricity", new BigDecimal(value));
                        break;
                }
            }
        }

        // 消息编号
        String messageNo = parsedMessage.getString("messageNo");
        // 防止重复请求
        String key = "property:post:" + messageNo;
        boolean isExist = redisTemplate.opsForValue().setIfAbsent(key, messageNo, 1, TimeUnit.HOURS);
        if (!isExist) {
            log.info("重复请求: {}", content);
            return;
        }

        // 柜机编号
        String cabinetNo = parsedMessage.getString("cabinetNo");
        // 充电宝编号
        String powerBankNo = parsedMessage.getString("powerBankNo");
        // 插槽编号
        String slotNo = parsedMessage.getString("slotNo");
        // 当前电量
        BigDecimal electricity = parsedMessage.getBigDecimal("electricity");

        if (StringUtils.isEmpty(cabinetNo)
                || StringUtils.isEmpty(powerBankNo)
                || StringUtils.isEmpty(slotNo)
                || null == electricity) {
            log.info("参数为空: {}", content);
            return;
        }

        // 获取柜机
        Cabinet cabinet = cabinetService.getBtCabinetNo(cabinetNo);
        // 获取充电宝
        PowerBank powerBank = powerBankService.getByPowerBankNo(powerBankNo);

        // 更新充电宝状态
        // 状态（0:未投放 1：可用 2：已租用 3：充电中 4：故障）
        // 更新充电宝电量与状态
        powerBank.setElectricity(electricity);
        // 电量大于可用最低值
        if (electricity.subtract(DeviceConstants.ELECTRICITY_MIN).doubleValue() > 0) {
            // 可以借用
            powerBank.setStatus("1");
        } else {
            // 充电中
            powerBank.setStatus("3");
        }
        powerBankService.updateById(powerBank);

        // 更新柜机可用充电宝数量
        // 获取柜机插槽列表
        List<CabinetSlot> cabinetSlotList = cabinetSlotService.list(new LambdaQueryWrapper<CabinetSlot>()
                .eq(CabinetSlot::getCabinetId, cabinet.getId())
                .eq(CabinetSlot::getStatus, "1") // 状态（1：占用 0：空闲 2：锁定）
        );

        // 获取插槽对应的充电宝id列表
        List<Long> powerBankIdList = cabinetSlotList.stream()
                .filter(item -> null != item.getPowerBankId())
                .map(CabinetSlot::getPowerBankId)
                .collect(Collectors.toList());

        // 获取可用充电宝列表
        List<PowerBank> powerBankList = powerBankService.list(new LambdaQueryWrapper<PowerBank>()
                .in(PowerBank::getId, powerBankIdList)
                .eq(PowerBank::getStatus, "1"));

        // 更新柜机可用充电宝数量
        cabinet.setAvailableNum(powerBankList.size());
        cabinetService.updateById(cabinet);
    }
}