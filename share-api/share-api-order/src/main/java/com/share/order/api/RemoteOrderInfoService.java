package com.share.order.api;


import com.share.common.core.constant.SecurityConstants;
import com.share.common.core.constant.ServiceNameConstants;
import com.share.common.core.domain.R;
import com.share.order.domain.OrderInfo;
import com.share.order.domain.OrderSqlVo;
import com.share.order.factory.RemoteOrderInfoFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 用户服务
 *
 * @author share
 */
@FeignClient(contextId = "remoteOrderInfoService",
        value = ServiceNameConstants.ORDER_SERVICE,
        fallbackFactory = RemoteOrderInfoFallbackFactory.class)
public interface RemoteOrderInfoService {
    @GetMapping("/orderInfo/getNoFinishOrder/{userId}")
    R<OrderInfo> getNoFinishOrder(@PathVariable("userId") Long userId,@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/orderInfo/getByOrderNo/{orderNo}")
    R<OrderInfo> getByOrderNo(@PathVariable("orderNo") String orderNo,@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    // 传递过来sql语句，根据sql语句查询数据库得到报表数据
    @PostMapping("/orderInfo/getOrderCount")
    R getOrderCount(@RequestBody OrderSqlVo orderSqlVo,@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

}
