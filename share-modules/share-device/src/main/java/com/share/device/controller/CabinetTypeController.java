package com.share.device.controller;

import com.share.common.core.web.controller.BaseController;
import com.share.common.core.web.domain.AjaxResult;
import com.share.common.core.web.page.TableDataInfo;
import com.share.common.log.annotation.Log;
import com.share.common.log.enums.BusinessType;
import com.share.common.log.enums.OperatorType;
import com.share.common.security.annotation.RequiresPermissions;
import com.share.device.domain.CabinetType;
import com.share.device.service.ICabinetTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.aspectj.weaver.loadtime.Aj;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Tag(name = "柜机类型接口管理")
@RestController
@RequestMapping("/cabinetType")
public class CabinetTypeController extends BaseController {


//    官方推荐使用final +构造器注入，，在创建对象时就赋值，如 new user("zhangsan")
//    autowired 创建完再赋值，如 new user()  user.setName("zhangsan")
    private  final ICabinetTypeService cabinetTypeService;

    public CabinetTypeController(ICabinetTypeService cabinetTypeService) {
        this.cabinetTypeService = cabinetTypeService;
    }
    @Operation(summary = "柜机类型分页查询")
    @Log(title = "柜机类型", businessType = BusinessType.QUERY)
    @GetMapping("/list")
    public TableDataInfo list(CabinetType cabinetType) {
        startPage();
        List<CabinetType> list = cabinetTypeService.selectCabinetTypeList(cabinetType);
        return getDataTable(list);
    }

    @Operation(summary = "根据ID查询详情")
    @GetMapping("{id}")
    public AjaxResult getCabinetType(@PathVariable Long id){
//        mybatisPuls 获取对象ById
        return success(cabinetTypeService.getById(id));
    }


    @Operation(summary = "添加柜机类型")
    @RequiresPermissions("device:cabinetType:add")
    @PostMapping
//    mybatisPuls 添加对象
    public AjaxResult add(@RequestBody CabinetType cabinetType) {
        return toAjax(cabinetTypeService.save(cabinetType));
    }


    @Operation(summary = "修改柜机")
    @PutMapping
    public AjaxResult updaet(@RequestBody CabinetType cabinetType){
        return toAjax(cabinetTypeService.updateById(cabinetType));
    }

    @Operation(summary = "删除柜机")
    @DeleteMapping("/{ids}")
    public AjaxResult delete(@RequestBody Long[] ids){
        return toAjax(cabinetTypeService.removeByIds(Arrays.asList(ids)));
    }


    @Operation(summary = "查询所有")
    @GetMapping("/getCabinetTypeList")
    public AjaxResult getCabinetTypeList(){
        return success(cabinetTypeService.list());
    }

}