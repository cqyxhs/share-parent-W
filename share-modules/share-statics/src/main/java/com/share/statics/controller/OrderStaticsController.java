package com.share.statics.controller;

import com.share.common.core.constant.SecurityConstants;
import com.share.common.core.domain.R;
import com.share.common.core.web.controller.BaseController;
import com.share.common.core.web.domain.AjaxResult;
import com.share.order.api.RemoteOrderInfoService;
import com.share.order.domain.OrderSqlVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Tag(name = "AI数据统计")
@RestController
@RequestMapping("/statics")
public class OrderStaticsController extends BaseController {

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private RemoteOrderInfoService orderInfoService;

    @GetMapping("/orderData")
    public AjaxResult generate(@RequestParam(value = "message", defaultValue = "hello")
                               String message) {
        //使用restTemplate调用ai接口获取生成的sql语句
        String sql = restTemplate.getForObject("http://localhost:8899/ai/generate?message="+message, String.class);
        //远程调用根据语句查询数据
        OrderSqlVo orderSqlVo = new OrderSqlVo();
        orderSqlVo.setSql(sql);
        R<Map<String, Object>> result = orderInfoService.getOrderCount(orderSqlVo, SecurityConstants.INNER);
        Map<String, Object> map = result.getData();
        return success(map);
    }

}