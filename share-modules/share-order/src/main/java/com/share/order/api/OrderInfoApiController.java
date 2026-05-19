package com.share.order.api;

import com.share.common.core.domain.R;
import com.share.common.core.web.controller.BaseController;
import com.share.common.core.web.domain.AjaxResult;
import com.share.common.security.annotation.RequiresLogin;
import com.share.common.security.utils.SecurityUtils;
import com.share.order.domain.OrderInfo;
import com.share.order.domain.OrderSqlVo;
import com.share.order.service.IOrderInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "订单接口管理")
@RestController
@RequestMapping("/orderInfo")
public class OrderInfoApiController extends BaseController
{
    @Resource
    private IOrderInfoService orderInfoService;

    //小程序使用
    @Operation(summary = "获取未完成订单")
    @RequiresLogin
    @GetMapping("/getNoFinishOrder")
    public AjaxResult getNoFinishOrder() {
        OrderInfo orderInfo = orderInfoService.getNoFinishOrder(SecurityUtils.getUserId());
        return success(orderInfo != null ? orderInfo : new OrderInfo());
    }

    //远程调用
    @Operation(summary = "获取未完成订单")
    @GetMapping("/getNoFinishOrder/{userId}")
    public R<OrderInfo> getNoFinishOrder(@PathVariable Long userId) {
        return R.ok(orderInfoService.getNoFinishOrder(userId));
    }

    @Operation(summary = "获取订单详细信息")
    @RequiresLogin
    @GetMapping(value = "/getOrderInfo/{id}")
    public AjaxResult getOrderInfo(@PathVariable("id") Long id)
    {
        return success(orderInfoService.selectOrderInfoById(id));
    }

    @PostMapping(value = "/getOrderCount")
    public R getOrderCount(@RequestBody OrderSqlVo orderSqlVo) {
        Map<String, Object> map = orderInfoService.getOrderCount(orderSqlVo.getSql());
        return R.ok(map);
    }
}
