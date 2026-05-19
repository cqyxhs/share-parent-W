package com.share.device.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.share.device.domain.Region;
import com.share.device.mapper.RegionMapper;
import com.share.device.service.IRegionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RegionServiceImpl extends ServiceImpl<RegionMapper, Region> implements IRegionService
{
    @Resource
    private RegionMapper regionMapper;


    @Override
    public List<Region> treeSelect(String parentCode) {
        List<Region> regionList = regionMapper.selectList(new LambdaQueryWrapper<Region>().eq(Region::getParentCode, parentCode));
//查子地区是否为空
        if (!CollectionUtils.isEmpty(regionList)) {
            // 1. 收集所有 code
            List<String> codes = regionList.stream()
                    .map(Region::getCode)
                    .collect(Collectors.toList());

            // 2. 一次查询：查出所有 parentCode 在 codes 中的记录（只需要 parentCode 字段）
            List<Region> children = regionMapper.selectList(
                    new LambdaQueryWrapper<Region>()
                            .in(Region::getParentCode, codes)
                            .select(Region::getParentCode)  // 只需要这一个字段
            );

            // 3. 提取有子节点的 parentCode 集合
            Set<String> codesWithChildren = children.stream()
                    .map(Region::getParentCode)
                    .collect(Collectors.toSet());

            // 4. 批量设置标志（内存操作，无 SQL）
            regionList.forEach(item ->
                    item.setHasChildren(codesWithChildren.contains(item.getCode()))
            );
        }

        return regionList;

    }



    @Override
    public String getNameByCode(String code) {
        if (StringUtils.isEmpty(code)) {
            return "";
        }
        Region region = regionMapper.selectOne(new LambdaQueryWrapper<Region>().eq(Region::getCode,code).select(Region::getName));
        if(null != region) {
            return region.getName();
        }
        return "";
    }
}