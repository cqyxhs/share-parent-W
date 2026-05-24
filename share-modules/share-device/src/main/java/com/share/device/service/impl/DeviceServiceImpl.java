package com.share.device.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.share.common.core.constant.SecurityConstants;
import com.share.common.core.context.SecurityContextHolder;
import com.share.common.core.domain.R;
import com.share.common.core.exception.ServiceException;
import com.share.common.core.utils.StringUtils;
import com.share.common.core.utils.bean.BeanUtils;
import com.share.common.security.utils.SecurityUtils;
import com.share.device.domain.*;
import com.share.device.emqx.EmqxClientWrapper;
import com.share.device.emqx.constant.EmqxConstants;
import com.share.device.service.*;
import com.share.order.api.RemoteOrderInfoService;
import com.share.order.domain.OrderInfo;
import com.share.rules.api.RemoteFeeRuleService;
import com.share.rules.domain.FeeRule;
import com.share.system.api.model.LoginUser;
import com.share.user.api.RemoteUserInfoService;
import com.share.user.domain.UserInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DeviceServiceImpl implements IDeviceService {

    @Resource
    private IStationService stationService;

    @Resource
    private ICabinetService cabinetService;

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private IMapService mapService;

    @Resource
    private RemoteFeeRuleService remoteFeeRuleService;

    @Resource
    private RemoteOrderInfoService remoteOrderInfoService;

    @Resource
    private RemoteUserInfoService remoteUserInfoService;

    @Resource
    private EmqxClientWrapper emqxClientWrapper;

    @Resource
    private ICabinetSlotService cabinetSlotService;

    @Resource
    private IPowerBankService powerBankService;

    @Override
    public List<StationVo> nearbyStation(String latitude, String longitude, Integer searchH5Radius) {
        //坐标，确定中心点
        // GeoJsonPoint(double x, double y) x 表示经度，y 表示纬度。
        GeoJsonPoint geoJsonPoint = new GeoJsonPoint(Double.parseDouble(longitude), Double.parseDouble(latitude));
        //画圈的半径,50km范围
        Distance d = new Distance(searchH5Radius, Metrics.KILOMETERS);
        //画了一个圆圈
        Circle circle = new Circle(geoJsonPoint, d);
        //条件排除自己
        Query query = Query.query(Criteria.where("location").withinSphere(circle));
//       //获取所有站点的数据（根据站点id）
        List<StationLocation> stationLocationList = this.mongoTemplate.find(query, StationLocation.class);
        if (CollectionUtils.isEmpty(stationLocationList)) return null;

        //组装数据
        List<Long> stationIdList =stationLocationList.stream().map(StationLocation::getStationId).collect(Collectors.toList());
        //获取站点列表
        List<Station> stationList = stationService.list(new LambdaQueryWrapper<Station>().in(Station::getId, stationIdList).isNotNull(Station::getCabinetId));

        //获取柜机id列表
        List<Long> cabinetIdList = stationList.stream().map(Station::getCabinetId).collect(Collectors.toList());
        //获取柜机id与柜机信息Map
        Map<Long, Cabinet> cabinetIdToCabinetMap = cabinetService.listByIds(cabinetIdList).stream().collect(Collectors.toMap(Cabinet::getId, Cabinet -> Cabinet));

        //获取柜机id列表
        List<Long> feeRuleIdList = stationList.stream().map(Station::getFeeRuleId).collect(Collectors.toList());

        //获取柜机id与柜机信息Map 机柜id与费用规则信息
        Map<Long, FeeRule> feeRuleIdToFeeRuleMap = remoteFeeRuleService.getFeeRuleList(feeRuleIdList, SecurityConstants.INNER).getData().stream().collect(Collectors.toMap(FeeRule::getId, FeeRule -> FeeRule));

        List<StationVo> stationVoList = new ArrayList<>();
        stationList.forEach(item -> {
            // 计算距离
            Double distance = mapService.calculateDistance(longitude, latitude, item.getLongitude().toString(), item.getLatitude().toString());

            //属性拷贝到映射对象
            StationVo stationVo = new StationVo();
            BeanUtils.copyProperties(item, stationVo);
            stationVo.setDistance(distance);

            // 获取柜机信息
            Cabinet cabinet = cabinetIdToCabinetMap.get(item.getCabinetId());

            //可用充电宝数量大于0，可借用
            if(cabinet.getAvailableNum() > 0) {
                stationVo.setIsUsable("1");
            } else {
                stationVo.setIsUsable("0");
            }
            // 获取空闲插槽数量大于0，可归还
            if (cabinet.getFreeSlots() > 0) {
                stationVo.setIsReturn("1");
            } else {
                stationVo.setIsReturn("0");
            }

            // 获取费用规则
            FeeRule feeRule = feeRuleIdToFeeRuleMap.get(item.getFeeRuleId());

            //设置返回对象中的费用规则
            stationVo.setFeeRule(feeRule.getDescription());

            //封装列表
            stationVoList.add(stationVo);
        });
        return stationVoList;
    }


    @Override
    public StationVo getStation(Long id, String latitude, String longitude) {
        Station station = stationService.getById(id);
        StationVo stationVo = new StationVo();
        BeanUtils.copyProperties(station, stationVo);
        // 计算距离
        Double distance = mapService.calculateDistance(longitude, latitude, station.getLongitude().toString(), station.getLatitude().toString());
        stationVo.setDistance(distance);

        // 获取柜机信息
        Cabinet cabinet = cabinetService.getById(station.getCabinetId());
        //可用充电宝数量大于0，可借用
        if(cabinet.getAvailableNum() > 0) {
            stationVo.setIsUsable("1");
        } else {
            stationVo.setIsUsable("0");
        }
        // 获取空闲插槽数量大于0，可归还
        if (cabinet.getFreeSlots() > 0) {
            stationVo.setIsReturn("1");
        } else {
            stationVo.setIsReturn("0");
        }

        // 获取费用规则
        FeeRule feeRule = remoteFeeRuleService.getFeeRule(station.getFeeRuleId(),SecurityConstants.INNER).getData();
        stationVo.setFeeRule(feeRule.getDescription());
        return stationVo;
    }

    @Override
    public ScanChargeVo scanCharge(String cabinetNo) {
        // 获取当前登录用户信息
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            throw new ServiceException("用户未登录");
        }

        // 扫码充电返回对象
        ScanChargeVo scanChargeVo = new ScanChargeVo();

        //免押金判断
        R<UserInfo> userInfoResult =  remoteUserInfoService.getInfo(SecurityContextHolder.getUserId(), SecurityConstants.INNER);
        System.out.println("code:"+userInfoResult.getCode());
        System.out.println("msg:"+userInfoResult.getMsg());
        System.out.println("data:"+userInfoResult.getData());

        if (R.FAIL == userInfoResult.getCode()) {
            throw new ServiceException(userInfoResult.getMsg());
        }
        UserInfo userInfo = userInfoResult.getData();
        if (null == userInfo) {
            throw new ServiceException("获取用户信息失败");
        }
        if("0".equals(userInfo.getDepositStatus())) {
            throw new ServiceException("未申请免押金使用");
        }

        R<OrderInfo> orderInfoResult = remoteOrderInfoService.getNoFinishOrder(SecurityContextHolder.getUserId(), SecurityConstants.INNER);
        if (R.FAIL == orderInfoResult.getCode()) {
            throw new ServiceException(orderInfoResult.getMsg());
        }
        OrderInfo orderInfo = orderInfoResult.getData();
        if(null != orderInfo) {
            if("0".equals(orderInfo.getStatus())) {
                scanChargeVo.setStatus("2");
                scanChargeVo.setMessage("有未归还充电宝，请归还后使用");
                return scanChargeVo;
            }
            if("1".equals(orderInfo.getStatus())) {
                scanChargeVo.setStatus("3");
                scanChargeVo.setMessage("有未支付订单，去支付");
                return scanChargeVo;
            }
        }

        // 获取可用充电宝信息
        AvailablePowerBankVo availablePowerBankVo = this.checkAvailablePowerBank(cabinetNo);
        if(null == availablePowerBankVo) {
            throw new ServiceException("无可用充电宝");
        }
        if(!StringUtils.isEmpty(availablePowerBankVo.getErrMessage())) {
            throw new ServiceException(availablePowerBankVo.getErrMessage());
        }


        /*
        {"mNo":"112233","cNo":"xgxgxxxg","pNo":"gg001","sNo":"1","userId":1}
         */
        // 生成借取指令，弹出充电宝
        JSONObject object = new JSONObject();
        object.put("userId", SecurityContextHolder.getUserId());
        object.put("mNo", "mm"+ RandomUtil.randomString(8));
        object.put("cNo", cabinetNo);
        object.put("pNo", availablePowerBankVo.getPowerBankNo());
        object.put("sNo", availablePowerBankVo.getSlotNo());
        String topic = String.format(EmqxConstants.TOPIC_SCAN_SUBMIT, cabinetNo);
        emqxClientWrapper.publish(topic, object.toJSONString());

        scanChargeVo.setStatus("1");
        return scanChargeVo;
    }

    /**
     * 根据柜机编号获取一个可用最优的充电宝
     */
    public AvailablePowerBankVo checkAvailablePowerBank(String cabinetNo) {
        AvailablePowerBankVo availableProwerBankVo = new AvailablePowerBankVo();

        Cabinet cabinet = cabinetService.getOne(new LambdaQueryWrapper<Cabinet>().eq(Cabinet::getCabinetNo, cabinetNo));
        if(cabinet.getAvailableNum() == 0) {
            availableProwerBankVo.setErrMessage("无可用充电宝");
            return availableProwerBankVo;
        }
        // 获取插槽列表
        List<CabinetSlot> cabinetSlotList = cabinetSlotService.list(new LambdaQueryWrapper<CabinetSlot>()
                .eq(CabinetSlot::getCabinetId, cabinet.getId())
                .eq(CabinetSlot::getStatus, "1") // 状态（1：占用 0：空闲 2：锁定）
        );
        // 获取插槽对应的充电宝id列表
        List<Long> powerBankIdList = cabinetSlotList.stream().filter(item -> null != item.getPowerBankId()).map(CabinetSlot::getPowerBankId).collect(Collectors.toList());
        //获取可用充电宝列表
        List<PowerBank> powerBankList = CollectionUtils.isEmpty(powerBankIdList) ? new ArrayList<>() : 
            powerBankService.list(new LambdaQueryWrapper<PowerBank>()
                    .in(PowerBank::getId, powerBankIdList)
                    .eq(PowerBank::getStatus, "1"));
        if(CollectionUtils.isEmpty(powerBankList)) {
            availableProwerBankVo.setErrMessage("无可用充电宝");
            return availableProwerBankVo;
        }
        // 根据电量降序排列
        if(powerBankList.size() > 1) {
            powerBankList.sort((o1, o2) -> o2.getElectricity().compareTo(o1.getElectricity()));
        }
        // 获取电量最多的充电宝
        PowerBank powerBank = powerBankList.get(0);
        // 获取电量最多的充电宝插槽信息
        CabinetSlot cabinetSlot = cabinetSlotList.stream()
                .filter(item -> null != item.getPowerBankId() && item.getPowerBankId().equals(powerBank.getId()))
                .toList().get(0);
        //锁定柜机卡槽
        cabinetSlot.setStatus("2");
        cabinetSlotService.updateById(cabinetSlot);

        // 设置返回对象
        availableProwerBankVo.setPowerBankNo(powerBank.getPowerBankNo());
        availableProwerBankVo.setSlotNo(cabinetSlot.getSlotNo());
        return availableProwerBankVo;

    }
}