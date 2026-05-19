package com.share.device.controller;

import com.share.common.core.web.controller.BaseController;
import com.share.common.core.web.domain.AjaxResult;
import com.share.common.core.web.page.TableDataInfo;
import com.share.common.security.utils.SecurityUtils;
import com.share.device.domain.Cabinet;
import com.share.device.service.ICabinetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Tag(name = "充电宝柜机接口管理")
@RestController
@RequestMapping("/cabinet")
public class CabinetController extends BaseController
{
    @Resource
    private ICabinetService cabinetService;

    /**
     * 查询充电宝柜机列表
     */
    @Operation(summary = "查询充电宝柜机列表")
    @GetMapping("/list")
    public TableDataInfo list(Cabinet cabinet) {
        startPage();
        List<Cabinet> list = cabinetService.selectCabinetList(cabinet);
        return getDataTable(list);
    }


    @Operation(summary ="根据ID查询详情")
    @GetMapping("{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(cabinetService.getAllInfo(id));
    }

    @Operation(summary = "新增充电宝柜机")
    @PostMapping
    public AjaxResult add(@RequestBody Cabinet cabinet) {
        return toAjax(cabinetService.save(cabinet));}

    @Operation(summary = "修改充电宝柜机")
    @PutMapping
    public AjaxResult edit(@RequestBody Cabinet cabinet) {
        return toAjax(cabinetService.updateById(cabinet));
    }

    @Operation(summary = "删除充电宝柜机")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(cabinetService.removeBatchByIds(Arrays.asList(ids)));
}
    @Operation(summary = "查询未使用的充电宝柜机列表")
    @GetMapping("/searchNoUseList/{keyword}")
    public AjaxResult searchNoUseList(@PathVariable String keyword){
        return success(cabinetService.searchNoUseList(keyword));
    }



}
