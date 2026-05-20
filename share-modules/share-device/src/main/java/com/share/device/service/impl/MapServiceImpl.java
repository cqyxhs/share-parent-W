package com.share.device.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.share.common.core.exception.ServiceException;
import com.share.device.service.IMapService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
public class MapServiceImpl implements IMapService {

    @Resource
    private RestTemplate restTemplate;
    @Value("${tencent.map.key}")
    private String key = "your-key";

    @Override
    public Double calculateDistance(String startLongitude,String startLatitude,String endLongitude,String endLatitude) {
//        String url = "https://apis.map.qq.com/ws/direction/v1/walking/?from={from}&to={to}&key={key}";
//
//        Map<String, String> map = new HashMap<>();
//        map.put("from", startLatitude + "," + startLongitude);
//        map.put("to", endLatitude + "," + endLongitude);
//        map.put("key", key);
//
//        JSONObject result = restTemplate.getForObject(url, JSONObject.class, map);
//        System.out.println("result:"+result.toJSONString());
//        if(result.getIntValue("status") != 0) {
//            // result:{"message":"此key每日调用量已达到上限","request_id":"e55356a31f1f4d469e038abeb85c34f5","status":121}
//            throw new ServiceException("地图服务调用失败");
//        }
//
//        //返回第一条最佳线路
//        JSONObject route = result.getJSONObject("result").getJSONArray("routes").getJSONObject(0);
//        // 单位：米
//        return route.getBigDecimal("distance").doubleValue();

        Random random = new Random();
        BigDecimal randomDouble = BigDecimal.valueOf(random.nextDouble(100));

        BigDecimal roundedvalue = randomDouble.setScale(1, RoundingMode.HALF_UP);
        return roundedvalue.doubleValue();


}}
