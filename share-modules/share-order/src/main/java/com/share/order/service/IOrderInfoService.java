package com.share.order.service;

import com.share.order.domain.EndOrderVo;
import com.share.order.domain.OrderInfo;
import com.share.order.domain.SubmitOrderVo;

import java.util.List;
import java.util.Map;

public interface IOrderInfoService {
    //获取用户的未完成订单
    OrderInfo getNoFinishOrder(Long userId);

    OrderInfo selectOrderInfoById(Long id);

    void endOrder(EndOrderVo endOrderVo);

    Long saveOrder(SubmitOrderVo orderForm);

    void processPaySucess(String orderNo);

    Map<String, Object> getOrderCount(String sql);



    List<OrderInfo> getOrderInfoListByUserId(Long userId);
}
