package com.share.device.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.share.device.domain.Cabinet;
import com.share.device.domain.Station;
import com.share.device.domain.StationLocation;
import com.share.device.mapper.StationMapper;
import com.share.device.repository.StationLocationRepository;
import com.share.device.service.ICabinetService;
import com.share.device.service.IRegionService;
import com.share.device.service.IStationService;
import jakarta.annotation.Resource;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StationServiceImpl extends ServiceImpl<StationMapper, Station> implements IStationService {
    @Resource
    private StationMapper stationMapper;
    @Resource
    private ICabinetService cabinetService;

    @Resource
    private IRegionService regionService;

    @Resource
    private StationLocationRepository stationLocationRepository;

    @Override
    public List<Station> selectStationList(Station station) {
        List<Station> list = stationMapper.selectStationList(station);
//        获取每个station中对于柜机的编号，当前station只有柜机id，根据柜机id查找柜机编号
        List<Long> cabinetIdList = list.stream().map(Station::getCabinetId).collect(Collectors.toList());

        Map<Long, String> cabinetIdToCabinetNoMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(cabinetIdList)) {
//            从数据库中批量查询对应的 Cabinet（机柜）信息
            List<Cabinet> cabinetList = cabinetService.list(new LambdaQueryWrapper<Cabinet>().in(Cabinet::getId, cabinetIdList));
            //            键：机柜 ID
            //            值：机柜编号
            cabinetIdToCabinetNoMap = cabinetList.stream().collect(Collectors.toMap(Cabinet::getId, Cabinet::getCabinetNo));
        }
        //        拿出编号给对应id的station中
        for (Station item : list) {
            item.setCabinetNo(cabinetIdToCabinetNoMap.get(item.getCabinetId()));
        }
//        放回station列表
        return list;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public int saveStation(Station station) {

        String provinceName = regionService.getNameByCode(station.getProvinceCode());
        String cityName = regionService.getNameByCode(station.getCityCode());
        String districtName = regionService.getNameByCode(station.getDistrictCode());
        station.setFullAddress(provinceName + cityName + districtName + station.getAddress());
        this.save(station);

//
//        //同步站点位置信息到MongoDB
        StationLocation stationLocation = stationLocationRepository.getByStationId(station.getId());
        stationLocation.setLocation(new GeoJsonPoint(station.getLongitude().doubleValue(),station.getLatitude().doubleValue()));
        stationLocation.setCreateTime(new Date());
        stationLocationRepository.save(stationLocation);

        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateStation(Station station) {
        String provinceName = regionService.getNameByCode(station.getProvinceCode());
        String cityName = regionService.getNameByCode(station.getCityCode());
        String districtName = regionService.getNameByCode(station.getDistrictCode());
        station.setFullAddress(provinceName + cityName + districtName + station.getAddress());
        this.updateById(station);

        //同步站点位置信息到MongoDB
        StationLocation stationLocation = stationLocationRepository.getByStationId(station.getId());
        stationLocation.setLocation(new GeoJsonPoint(station.getLongitude().doubleValue(), station.getLatitude().doubleValue()));
        stationLocationRepository.save(stationLocation);
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean removeByIds(Collection<?> list) {
        for (Object id : list) {
            stationLocationRepository.deleteByStationId(Long.parseLong(id.toString()));
        }
        return super.removeByIds(list);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int setData(Station station) {
        this.updateById(station);

        //更正柜机使用状态
        Cabinet cabinet = cabinetService.getById(station.getCabinetId());
        cabinet.setStatus("1");
        cabinetService.updateById(cabinet);
        return 1;
    }

    @Override
    public void updateData() {
        List<Station> stationList = this.list();
        for (Station station : stationList) {
            StationLocation stationLocation = stationLocationRepository.getByStationId(station.getId());
            if(stationLocation == null) {
                stationLocation = new StationLocation();
                stationLocation.setId(ObjectId.get().toString());
                stationLocation.setStationId(station.getId());
                stationLocation.setLocation(new GeoJsonPoint(station.getLongitude().doubleValue(), station.getLatitude().doubleValue()));
                stationLocation.setCreateTime(new Date());
                stationLocationRepository.save(stationLocation);
            }
        }
    }

    @Override
    public Station getByCabinetId(Long cabinetId) {
        return stationMapper.selectOne(new LambdaQueryWrapper<Station>().eq(Station::getCabinetId, cabinetId));
    }
}